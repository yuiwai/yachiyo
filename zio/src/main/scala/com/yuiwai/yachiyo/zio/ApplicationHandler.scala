package com.yuiwai.yachiyo.zio

import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui._
import com.yuiwai.yachiyo.zio.PresenterHandler.{PresenterCallback, PresenterCommand}
import com.yuiwai.yachiyo.zio.SceneHandler.{SceneCommand, SceneEnv}
import com.yuiwai.yachiyo.zio.ViewHandler.{ViewCallback, ViewCommand}
import zio.stream._
import zio._

object ApplicationHandler {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  final case class ViewCallbackWrap(viewCallback: ViewCallback) extends ApplicationCommand
  case object Terminate extends ApplicationCommand

  final case class AppEnv(application: ui.Application, appState: AppState) {
    def currentSceneSuite: ZIO[Any, Nothing, SceneSuite] = appState.currentSceneKey.get.map(application.resolve)
    def resetRefs(suite: SceneSuite): ZIO[Any, Nothing, Unit] = for {
      _ <- appState.currentScene.set(suite.genScene()) *>
        appState.currentPresenter.set(suite.genPresenter()) *>
        appState.currentView.set(suite.genView())
    } yield ()
  }
  object AppEnv {
    def unsafeInit(application: Application) = apply(application, AppState.unsafeInit(application))
    def init(application: Application): ZIO[Any, Nothing, AppEnv] =
      AppState.init(application).map(appState => apply(application, appState))
  }

  final case class AppState(
    currentSceneKey: Ref[Int],
    currentScene: Ref[ui.Scene],
    currentPresenter: Ref[ui.Presenter],
    currentView: Ref[ui.View],
    appQueue: Queue[ApplicationCommand],
    sceneQueue: Queue[SceneCommand[_]],
    presenterQueue: Queue[PresenterCommand],
    viewQueue: Queue[ViewCommand]
  )
  object AppState extends DefaultRuntime {
    def unsafeInit(application: Application): AppState = unsafeRun(init(application))
    def init(application: Application): ZIO[Any, Nothing, AppState] = {
      val sceneSuit = application.initialSceneSuite
      for {
        currentSceneKey <- Ref.make(application.initialSceneSuiteKey)
        currentScene <- Ref.make(sceneSuit.genScene())
        currentPresenter <- Ref.make(sceneSuit.genPresenter())
        currentView <- Ref.make(sceneSuit.genView())
        appQueue <- Queue.unbounded[ApplicationCommand]
        sceneQueue <- Queue.unbounded[SceneCommand[_]]
        presenterQueue <- Queue.unbounded[PresenterCommand]
        viewQueue <- Queue.unbounded[ViewCommand]
      } yield new AppState(
        currentSceneKey,
        currentScene,
        currentPresenter,
        currentView,
        appQueue,
        sceneQueue,
        presenterQueue,
        viewQueue
      )
    }
  }

  private val sceneQueue = ZIO.access[AppEnv](_.appState.sceneQueue)
  private val presenterQueue = ZIO.access[AppEnv](_.appState.presenterQueue)
  private val viewQueue = ZIO.access[AppEnv](_.appState.viewQueue)
  def commandHandler(applicationCommand: ApplicationCommand): ZIO[AppEnv, Nothing, Unit] = applicationCommand match {
    case ApplicationHandler.SceneCallbackWrap(msg) => msg match {
      case ui.Initialized(state) => for {
        queue <- presenterQueue
        genPresenter <- ZIO.accessM[AppEnv](_.currentSceneSuite.map(_.genPresenter))
        _ <- queue.offer(PresenterHandler.Initialize(state, genPresenter))
      } yield ()
      case ui.StateChangedCallback(state) => for {
        queue <- ZIO.access[AppEnv](_.appState.presenterQueue)
        _ <- queue.offer(PresenterHandler.Update(state))
      } yield ()
      case ui.EventCallback(_) => UIO(())
      case ui.CleanedUp => for {
        queue <- presenterQueue
        _ <- queue.offer(PresenterHandler.Cleanup)
      } yield ()
      case ui.NextSceneCallback(sceneKey) => for {
        _ <- ZIO.accessM[AppEnv](_.appState.currentSceneKey.set(sceneKey))
        suite <- ZIO.accessM[AppEnv](_.currentSceneSuite)
        _ <- ZIO.accessM[AppEnv](_.resetRefs(suite))
        queue <- sceneQueue
        _ <- queue.offer(SceneHandler.Initialize(suite.genScene))
      } yield ()
      case ui.NoCallback => UIO(())
    }
    case ApplicationHandler.PresenterCallbackWrap(msg) => msg match {
      case PresenterHandler.Initialized(viewModel) => for {
        queue <- viewQueue
        genView <- ZIO.accessM[AppEnv](_.currentSceneSuite.map(_.genView))
        _ <- queue.offer(ViewHandler.Initialize(genView, viewModel))
      } yield ()
      case PresenterHandler.Updated(viewModel) => for {
        queue <- viewQueue
        _ <- queue.offer(ViewHandler.Update(viewModel))
      } yield ()
      case PresenterHandler.CleanedUp => for {
        queue <- viewQueue
        _ <- queue.offer(ViewHandler.Cleanup)
      } yield ()
    }
    case ApplicationHandler.ViewCallbackWrap(msg) => msg match {
      case ViewHandler.Initialized => for {
        queue <- sceneQueue
        _ <- queue.offer(SceneHandler.OnStart)
      } yield ()
      case ViewHandler.ExecutionCallback(command) => for {
        queue <- sceneQueue
        _ <- queue.offer(command)
      } yield ()
      case ViewHandler.CleanedUp => for {
        queue <- sceneQueue
        genScene <- ZIO.accessM[AppEnv](env => env.currentSceneSuite.map(_.genScene))
        _ <- queue.offer(SceneHandler.Initialize(genScene))

      } yield ()
    }
    case Terminate => UIO(())
  }

  val program: ZIO[AppEnv, Throwable, Unit] = for {
    sceneSuitRef <- ZIO.accessM[AppEnv](env => Ref.make(env.application.initialSceneSuite))
    sceneRef <- sceneSuitRef.get.flatMap(suite => Ref.make(suite.genScene()))
    appQueue <- Queue.unbounded[ApplicationCommand]
    sceneQueue <- Queue.unbounded[SceneCommand[_]]
    _ <- sceneRef.get.flatMap(scene =>
      SceneHandler.program(scene, sceneQueue).provide(SceneEnv(msg => appQueue.offer(SceneCallbackWrap(msg)))))
    _ <- Stream.fromQueue(appQueue).foreach(commandHandler).fork
  } yield ()
}

object SceneHandler {
  type GenScene = () => ui.Scene
  type Listener = SceneCallback => Unit

  final case class SceneEnv(listener: Listener)

  sealed trait SceneCommand[-T]
  final case class Initialize(genScene: GenScene) extends SceneCommand[Nothing]
  case object OnStart extends SceneCommand[Nothing]
  case object Cleanup extends SceneCommand[Nothing]
  final case class Execution[S <: ui.Scene](input: S#Command) extends SceneCommand[S]

  private def commandHandler[S <: ui.Scene](
    scene: S, state: S#State, sceneCommand: SceneCommand[_], listener: Listener
  ): S#State = sceneCommand match {
    case SceneHandler.Initialize(genScene) =>
      val s = genScene().initialState().asInstanceOf[S#State]
      listener(ui.Initialized(s))
      s
    case SceneHandler.Execution(input) =>
      scene.execute(state.asInstanceOf[scene.State], input.asInstanceOf[scene.Command])._1
    case SceneHandler.Cleanup =>
      scene.cleanup()
      listener(ui.CleanedUp)
      state
    case SceneHandler.OnStart =>
      // TODO 初期化後のハンドラ
      state
  }

  def program[S <: ui.Scene](scene: S, queue: Queue[SceneCommand[_]]): ZIO[SceneEnv, Throwable, Unit] = for {
    state <- Ref.make(scene.initialState())
    listener <- ZIO.access[SceneEnv](_.listener)
    _ <- queue.take.flatMap {
      command =>
        state.get.map { s =>
          val ss = commandHandler(scene, s, command, listener)
          if (s != ss) {
            listener(StateChangedCallback(ss))
            state.set(ss.asInstanceOf[scene.State])
          }
        }
    }.fork
  } yield ()
}

object PresenterHandler {
  type GenPresenter[M <: ViewModel] = () => ui.Presenter
  type Listener = PresenterCallback => Unit

  final case class PresenterEnv(listener: Listener)

  sealed trait PresenterCommand
  final case class Initialize[S <: ui.Scene, M <: ViewModel](initialState: S#State, genPresenter: GenPresenter[M]) extends PresenterCommand
  final case class Update[S <: ui.Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ViewModel) extends PresenterCallback
  case object CleanedUp extends PresenterCallback
  final case class Updated[M <: ViewModel](viewModel: ViewModel) extends PresenterCallback

  def program(presenter: ui.Presenter, queue: Queue[PresenterCommand]): ZIO[PresenterEnv, Nothing, Unit] = for {
    listener <- ZIO.access[PresenterEnv](_.listener)
    _ <- queue.take.map {
      case Initialize(initialState, genPresenter) =>
        val viewModel = presenter.setup(initialState.asInstanceOf[presenter.S#State])
        listener(Initialized(viewModel))
      case Update(state) =>
        val viewModel = presenter.updated(state.asInstanceOf[presenter.S#State], None)
        listener(Updated(viewModel))
      case Cleanup =>
        presenter.cleanup()
        listener(CleanedUp)
    }.fork
  } yield ()
}

object ViewHandler {
  type GenView = () => ui.View
  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ViewModel) extends ViewCommand
  final case class Update(viewModel: ViewModel) extends ViewCommand

  case object Cleanup extends ViewCommand
  sealed trait ViewCallback
  case object Initialized extends ViewCallback
  case object CleanedUp extends ViewCallback
  final case class ExecutionCallback(command: SceneCommand[_]) extends ViewCallback
}
