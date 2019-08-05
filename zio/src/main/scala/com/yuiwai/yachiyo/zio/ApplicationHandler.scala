package com.yuiwai.yachiyo.zio

import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.zio.ApplicationHandler.{ApplicationCommand, PresenterCallbackWrap, SceneCallbackWrap, ViewCallbackWrap}
import com.yuiwai.yachiyo.zio.PresenterHandler.{PresenterCallback, PresenterCommand, PresenterEnv}
import com.yuiwai.yachiyo.zio.SceneHandler.{SceneCommand, SceneEnv}
import com.yuiwai.yachiyo.zio.ViewHandler.{ViewCallback, ViewCommand, ViewEnv}
import zio._

object ApplicationHandler {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: ui.SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  final case class ViewCallbackWrap(viewCallback: ViewCallback) extends ApplicationCommand
  case object Terminate extends ApplicationCommand

  final case class AppEnv(application: ui.Application, appState: AppState) {
    def currentSceneSuite: ZIO[Any, Nothing, ui.SceneSuite] = appState.currentSceneKey.get.map(application.resolve)
    def resetRefs(suite: ui.SceneSuite): ZIO[Any, Nothing, Unit] = for {
      _ <- appState.currentScene.set(suite.genScene()) *>
        appState.currentPresenter.set(suite.genPresenter()) *>
        appState.currentView.set(suite.genView())
    } yield ()
  }
  object AppEnv {
    def unsafeInit(application: ui.Application) = apply(application, AppState.unsafeInit(application))
    def init(application: ui.Application): ZIO[Any, Nothing, AppEnv] =
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
    def unsafeInit(application: ui.Application): AppState = unsafeRun(init(application))
    def init(application: ui.Application): ZIO[Any, Nothing, AppState] = {
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
  def commandHandler(applicationCommand: ApplicationCommand): ZIO[AppEnv, Throwable, Unit] = applicationCommand match {
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
        queue <- sceneQueue
        _ <- queue.offer(SceneHandler.Cleanup)
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
        suite <- ZIO.accessM[AppEnv](_.currentSceneSuite)
        _ <- ZIO.accessM[AppEnv](_.resetRefs(suite))
        sceneQueue <- ZIO.access[AppEnv](_.appState.sceneQueue)
        _ <- sceneQueue.offer(SceneHandler.Initialize)
      } yield ()
    }
    case Terminate => UIO(())
  }

  val setup: ZIO[AppEnv, Throwable, Unit] = for {
    sceneRef <- ZIO.access[AppEnv](_.appState.currentScene)
    presenterRef <- ZIO.access[AppEnv](_.appState.currentPresenter)
    viewRef <- ZIO.access[AppEnv](_.appState.currentView)
    appQueue <- ZIO.access[AppEnv](_.appState.appQueue)
    sceneQueue <- ZIO.access[AppEnv](_.appState.sceneQueue)
    presenterQueue <- ZIO.access[AppEnv](_.appState.presenterQueue)
    viewQueue <- ZIO.access[AppEnv](_.appState.viewQueue)
    _ <- SceneHandler.program(sceneRef, sceneQueue).provide(SceneEnv(appQueue))
    _ <- PresenterHandler.program(presenterRef, presenterQueue).provide(PresenterEnv(appQueue))
    _ <- ViewHandler.program(viewRef, viewQueue).provide(ViewEnv(appQueue))
    _ <- sceneQueue.offer(SceneHandler.Initialize)
  } yield ()

  val program: ZIO[AppEnv, Throwable, Unit] = for {
    appQueue <- ZIO.access[AppEnv](_.appState.appQueue)
    _ <- setup
    f <- appQueue.take.flatMap(commandHandler).forever.fork
    _ <- f.join
  } yield ()
}

object SceneHandler {
  final case class SceneEnv(appQueue: Queue[ApplicationCommand])

  sealed trait SceneCommand[-T]
  case object Initialize extends SceneCommand[Nothing]
  case object OnStart extends SceneCommand[Nothing]
  case object Cleanup extends SceneCommand[Nothing]
  final case class Execution[S <: ui.Scene](input: S#Command) extends SceneCommand[S]

  private def commandHandler[S <: ui.Scene](
    scene: S, state: S#State, sceneCommand: SceneCommand[_], appQueue: Queue[ApplicationCommand]
  ) = sceneCommand match {
    case SceneHandler.Initialize =>
      val s = scene.initialState().asInstanceOf[S#State]
      appQueue.offer(SceneCallbackWrap(ui.Initialized(s))) *> UIO(s)
    case SceneHandler.Execution(input) =>
      scene.execute(state.asInstanceOf[scene.State], input.asInstanceOf[scene.Command]) match {
        case (s, _, n@ui.NextSceneCallback(_)) =>
          appQueue.offer(SceneCallbackWrap(n)) *> UIO(s)
        case (s, _, _) => UIO(s)
      }
    case SceneHandler.Cleanup =>
      scene.cleanup()
      appQueue.offer(SceneCallbackWrap(ui.CleanedUp)) *> UIO(state)
    case SceneHandler.OnStart =>
      // TODO 初期化後のハンドラ
      UIO(state)
  }

  def program[S <: ui.Scene](sceneRef: Ref[S], queue: Queue[SceneCommand[_]]): ZIO[SceneEnv, Throwable, Unit] = for {
    appQueue <- ZIO.access[SceneEnv](_.appQueue)
    _ <- queue.take.flatMap { command =>
      sceneRef.get.flatMap { scene =>
        Ref.make(scene.initialState()).flatMap { state =>
          state.get.flatMap { s =>
            commandHandler(scene, s, command, appQueue).flatMap { ss =>
              if (s != ss) {
                appQueue.offer(SceneCallbackWrap(ui.StateChangedCallback(ss))) *>
                  state.set(ss.asInstanceOf[scene.State])
              } else UIO(())
            }
          }
        }
      }
    }.forever.fork
  } yield ()
}

object PresenterHandler {
  type GenPresenter[M <: ui.ViewModel] = () => ui.Presenter

  final case class PresenterEnv(appQueue: Queue[ApplicationCommand])

  sealed trait PresenterCommand
  final case class Initialize[S <: ui.Scene, M <: ui.ViewModel](initialState: S#State, genPresenter: GenPresenter[M]) extends PresenterCommand
  final case class Update[S <: ui.Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ui.ViewModel) extends PresenterCallback
  case object CleanedUp extends PresenterCallback
  final case class Updated[M <: ui.ViewModel](viewModel: ui.ViewModel) extends PresenterCallback

  def program(presenterRef: Ref[ui.Presenter], queue: Queue[PresenterCommand]): ZIO[PresenterEnv, Nothing, Unit] = for {
    appQueue <- ZIO.access[PresenterEnv](_.appQueue)
    _ <- queue.take.flatMap { msg =>
      presenterRef.get.flatMap { presenter =>
        msg match {
          case Initialize(initialState, _) =>
            val viewModel = presenter.setup(initialState.asInstanceOf[presenter.S#State])
            appQueue.offer(PresenterCallbackWrap(Initialized(viewModel)))
          case Update(state) =>
            val viewModel = presenter.updated(state.asInstanceOf[presenter.S#State], None)
            appQueue.offer(PresenterCallbackWrap(Updated(viewModel)))
          case Cleanup =>
            presenter.cleanup()
            appQueue.offer(PresenterCallbackWrap(CleanedUp))
        }
      }
    }.forever.fork
  } yield ()
}

object ViewHandler extends DefaultRuntime {
  type GenView = () => ui.View

  final case class ViewEnv(appQueue: Queue[ApplicationCommand])

  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ui.ViewModel) extends ViewCommand
  final case class Update(viewModel: ui.ViewModel) extends ViewCommand

  case object Cleanup extends ViewCommand
  sealed trait ViewCallback
  case object Initialized extends ViewCallback
  case object CleanedUp extends ViewCallback
  final case class ExecutionCallback(command: SceneCommand[_]) extends ViewCallback

  def program(viewRef: Ref[ui.View], queue: Queue[ViewCommand]) = for {
    appQueue <- ZIO.access[ViewEnv](_.appQueue)
    _ <- queue.take.flatMap { msg =>
      viewRef.get.flatMap { view =>
        msg match {
          case ViewHandler.Initialize(_, viewModel) =>
            view.setup(viewModel.asInstanceOf[view.M],
              msg => unsafeRun(appQueue.offer(ViewCallbackWrap(ExecutionCallback(SceneHandler.Execution(msg)))))
            )
            appQueue.offer(ViewCallbackWrap(ViewHandler.Initialized))
          case ViewHandler.Update(viewModel) =>
            UIO(view.update(viewModel.asInstanceOf[view.M]))
          case ViewHandler.Cleanup =>
            view.cleanup()
            appQueue.offer(ViewCallbackWrap(ViewHandler.CleanedUp))
        }
      }
    }.forever.fork
  } yield ()
}
