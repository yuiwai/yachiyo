package com.yuiwia.yachiyo.zio

import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui._
import com.yuiwai.yachiyo.zio.ApplicationHandler._
import com.yuiwai.yachiyo.zio.PresenterHandler.{PresenterCommand, PresenterEnv}
import com.yuiwai.yachiyo.zio.SceneHandler.{SceneCommand, SceneEnv}
import com.yuiwai.yachiyo.zio.ViewHandler.{ViewCommand, ViewEnv}
import com.yuiwai.yachiyo.zio.{ApplicationHandler, PresenterHandler, SceneHandler, ViewHandler}
import com.yuiwia.yachiyo.zio.TestScene.AddOne
import utest._
import zio.{DefaultRuntime, Queue, Ref, ZIO}

object ApplicationHandlerSpec extends TestSuite with DefaultRuntime {
  val tests = Tests {
    "commandHandler" - {
      import ApplicationHandler.commandHandler
      val defaultEnv: AppEnv = AppEnv.unsafeInit(TestApp)

      def doCommand(command: ApplicationCommand)
        (appEnv: AppEnv = defaultEnv): Unit =
        unsafeRun(for {
          _ <- commandHandler(command).provide(appEnv)
        } yield ())

      def headOfSceneQueue(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.sceneQueue.take)
      } yield r).provide(env))

      def headOfPresenterQueue(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.presenterQueue.take)
      } yield r).provide(env))

      def headOfViewQueue(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.viewQueue.take)
      } yield r).provide(env))

      /*
      def currentScene(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.currentScene.get)
      } yield r).provide(env))

      def currentPresenter(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.currentPresenter.get)
      } yield r).provide(env))

      def currentView(env: AppEnv = defaultEnv) = unsafeRun((for {
        r <- ZIO.accessM[AppEnv](_.appState.currentView.get)
      } yield r).provide(env))
      */

      "sceneCallback" - {
        doCommand(SceneCallbackWrap(ui.Initialized(10: TestScene.State)))()
        headOfPresenterQueue().asInstanceOf[PresenterHandler.Initialize[_, _]].initialState ==> 10

        doCommand(SceneCallbackWrap(ui.StateChangedCallback(2: TestScene.State)))()
        headOfPresenterQueue() ==> PresenterHandler.Update(2: TestScene.State)

        doCommand(SceneCallbackWrap(ui.CleanedUp))()
        headOfPresenterQueue() ==> PresenterHandler.Cleanup

        doCommand(SceneCallbackWrap(ui.NextSceneCallback(2)))()
      }
      "presenterCallback" - {
        doCommand(PresenterCallbackWrap(PresenterHandler.Initialized(TestViewModel(5))))()
        headOfViewQueue().asInstanceOf[ViewHandler.Initialize].viewModel ==> TestViewModel(5)

        doCommand(PresenterCallbackWrap(PresenterHandler.Updated(TestViewModel(3))))()
        headOfViewQueue() ==> ViewHandler.Update(TestViewModel(3))

        doCommand(PresenterCallbackWrap(PresenterHandler.CleanedUp))()
        headOfViewQueue() ==> ViewHandler.Cleanup
      }
      "viewCallback" - {
        doCommand(ViewCallbackWrap(ViewHandler.Initialized))()
        headOfSceneQueue() ==> SceneHandler.OnStart

        doCommand(ViewCallbackWrap(ViewHandler.ExecutionCallback(SceneHandler.Execution(AddOne: TestScene.Command))))()
        headOfSceneQueue() ==> SceneHandler.Execution(AddOne: TestScene.Command)

        doCommand(ViewCallbackWrap(ViewHandler.CleanedUp))()
      }
      "terminate" - {
        doCommand(ApplicationHandler.Terminate)()
      }
    }
  }
}

object SceneHandlerSpec extends TestSuite with DefaultRuntime {
  val defaultEnv = unsafeRun(Queue.unbounded[ApplicationCommand].map(q => SceneEnv(q)))
  def doCommand(command: SceneCommand[_])(env: SceneEnv = defaultEnv): Unit =
    unsafeRun {
      for {
        queue <- Queue.unbounded[SceneCommand[_]]
        sceneRef <- Ref.make(TestScene)
        sceneStateRef <- Ref.make(TestScene.initialState(): TestScene.State)
        _ <- queue.offer(command)
        _ <- SceneHandler.program(sceneRef, sceneStateRef, queue).provide(env)
      } yield ()
    }
  def execute(command: TestScene.Command)(env: SceneEnv = defaultEnv): Unit =
    unsafeRun {
      for {
        queue <- Queue.unbounded[SceneCommand[_]]
        sceneRef <- Ref.make(TestScene)
        sceneStateRef <- Ref.make(TestScene.initialState(): TestScene.State)
        _ <- queue.offer(SceneHandler.Execution(command))
        _ <- SceneHandler.program(sceneRef, sceneStateRef, queue).provide(env)
      } yield ()
    }
  def headOfQueue(env: SceneEnv = defaultEnv) =
    unsafeRun(
      (for {
        queue <- ZIO.access[SceneEnv](_.appQueue)
        msg <- queue.take
      } yield msg).provide(env)
    )
  val tests = Tests {
    "initialize" - {
      doCommand(SceneHandler.Initialize)()
      Thread.sleep(10)
      headOfQueue() ==> SceneCallbackWrap(ui.Initialized(1: TestScene.State))
    }
    "execute command" - {
      execute(AddOne)()
      Thread.sleep(10)
      headOfQueue() ==> SceneCallbackWrap(StateChangedCallback(2: TestScene.State))
    }
    "cleanup" - {
      doCommand(SceneHandler.Cleanup)()
      Thread.sleep(10)
      headOfQueue() ==> SceneCallbackWrap(ui.CleanedUp)
    }
  }
}

object PresenterHandlerSpec extends TestSuite with DefaultRuntime {
  private val defaultEnv = unsafeRun(
    for {
      q <- Queue.unbounded[ApplicationCommand]
    } yield PresenterEnv(q)
  )
  def doCommand(command: PresenterCommand)(env: PresenterEnv = defaultEnv): Unit =
    unsafeRun {
      for {
        queue <- Queue.unbounded[PresenterCommand]
        ref <- Ref.make[ui.Presenter](TestPresenter)
        _ <- queue.offer(command)
        _ <- PresenterHandler.program(ref, queue).provide(env)
      } yield ()
    }
  def headOfQueue(env: PresenterEnv = defaultEnv) =
    unsafeRun(
      (for {
        queue <- ZIO.access[PresenterEnv](_.appQueue)
        msg <- queue.take
      } yield msg).provide(env)
    )
  val tests = Tests {
    "initialize" - {
      doCommand(PresenterHandler.Initialize[TestScene.type, TestViewModel](1, () => TestPresenter))()
      Thread.sleep(10)
      headOfQueue() ==> PresenterCallbackWrap(PresenterHandler.Initialized(TestViewModel(1)))
    }
    "update" - {
      doCommand(PresenterHandler.Update(2: TestScene.State))()
      Thread.sleep(10)
      headOfQueue() ==> PresenterCallbackWrap(PresenterHandler.Updated(TestViewModel(2)))
    }
    "cleanup" - {
      doCommand(PresenterHandler.Cleanup)()
      Thread.sleep(10)
      headOfQueue() ==> PresenterCallbackWrap(PresenterHandler.CleanedUp)
    }
  }
}

object ViewHandlerSpec extends TestSuite with DefaultRuntime {
  private val defaultEnv = unsafeRun(Queue.unbounded[ApplicationCommand].map(q => ViewEnv(q)))
  def doCommand(command: ViewCommand)(env: ViewEnv = defaultEnv) = unsafeRun(for {
    queue <- Queue.unbounded[ViewCommand]
    ref <- Ref.make[ui.View](TestView)
    _ <- queue.offer(command)
    _ <- ViewHandler.program(ref, queue).provide(env)
  } yield ())
  def headOfQueue(env: ViewEnv = defaultEnv) =
    unsafeRun((for {
      queue <- ZIO.access[ViewEnv](_.appQueue)
      msg <- queue.take
    } yield msg).provide(env))
  val tests = Tests {
    "initialize" - {
      doCommand(ViewHandler.Initialize(() => TestView, TestViewModel(1)))()
      headOfQueue() ==> ViewCallbackWrap(ViewHandler.Initialized)
    }
    "update" - {
      doCommand(ViewHandler.Update(TestViewModel(5)))()
    }
    "cleanup" - {
      doCommand(ViewHandler.Cleanup)()
      headOfQueue() ==> ViewCallbackWrap(ViewHandler.CleanedUp)
    }
  }
}

object TestApp extends ui.Application {
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    1 -> SceneSuite(
      () => TestScene,
      () => TestPresenter,
      () => TestView
    ),
    2 -> SceneSuite(
      () => TestScene2,
      () => TestPresenter2,
      () => TestView2
    )
  )
  override def initialSceneSuiteKey: Int = 1
}
object TestScene extends ui.Scene {
  override type State = Int
  override type Command = Cmd
  override type Event = Evt

  sealed trait Cmd
  case object AddOne extends Cmd

  sealed trait Evt
  case object DefaultEvt extends Evt

  override def initialState(): Int = 1
  override def execute(state: Int, input: Cmd): (Int, Evt, SceneCallback) = input match {
    case AddOne => (state + 1, DefaultEvt, NoCallback)
    case _ => (1, DefaultEvt, NoCallback)
  }
}
object TestScene2 extends ui.Scene {
  override type State = Int
  override type Command = Cmd
  override type Event = Evt

  sealed trait Cmd
  case object AddOne extends Cmd

  sealed trait Evt
  case object DefaultEvt extends Evt

  override def initialState(): Int = 1
  override def execute(state: Int, input: Cmd): (Int, Evt, SceneCallback) = input match {
    case AddOne => (state + 1, DefaultEvt, NoCallback)
    case _ => (1, DefaultEvt, NoCallback)
  }
}

object TestPresenter extends ui.Presenter {
  override type S = TestScene.type
  override type M = TestViewModel
  override def updated(state: Int, prevModel: TestPresenter.Prev): TestViewModel = TestViewModel(state)
}
object TestPresenter2 extends ui.Presenter {
  override type S = TestScene.type
  override type M = TestViewModel
  override def updated(state: Int, prevModel: TestPresenter.Prev): TestViewModel = TestViewModel(state)
}

final case class TestViewModel(value: Int) extends ui.ViewModel
object TestView extends ui.View {
  override type S = TestScene.type
  override type M = TestViewModel
  override def setup(viewModel: TestViewModel, listener: TestView.Listener): Unit = ()
  override def update(viewModel: TestViewModel): Unit = ()
  override def cleanup(): Unit = ()
}
object TestView2 extends ui.View {
  override type S = TestScene.type
  override type M = TestViewModel
  override def setup(viewModel: TestViewModel, listener: TestView.Listener): Unit = ()
  override def update(viewModel: TestViewModel): Unit = ()
  override def cleanup(): Unit = ()
}

trait CommonTestUtil {
  def sleep(millis: Long): Unit = Thread.sleep(millis)
}
