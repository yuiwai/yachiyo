package com.yuiwai.yachiyo.plain

import com.yuiwai.yachiyo.plain.ApplicationHandler.{AppState, PresenterCallbackWrap, SceneCallbackWrap, ViewCallbackWrap}
import com.yuiwai.yachiyo.plain.PresenterHandler.PresenterCallback
import com.yuiwai.yachiyo.plain.SceneHandler.{Execution, SceneCommand}
import com.yuiwai.yachiyo.plain.ViewHandler.{ExecutionCallback, ViewCallback}
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.{CleanedUp, Initialized, _}

object ApplicationHandler {
  private var internalAppState: Option[AppState] = None
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  final case class ViewCallbackWrap(viewCallback: ViewCallback) extends ApplicationCommand
  final case class AppState(
    sceneSuiteMap: Map[Int, SceneSuite],
    currentSceneSuite: SceneSuite,
    scene: Scene,
    state: Scene#State,
    presenter: Presenter,
    view: View,
    appCommandCallback: ApplicationCommand => Unit) {
    def nextScene(sceneKey: Int): AppState = copy(currentSceneSuite = sceneSuiteMap(sceneKey))
  }
  object AppState {
    def init(application: ui.Application, appCommandCallback: ApplicationCommand => Unit): AppState = {
      val sceneSuite = application.initialSceneSuite
      val scene = sceneSuite.genScene()
      apply(
        application.sceneSuiteMap,
        sceneSuite,
        scene,
        scene.initialState(),
        sceneSuite.genPresenter(),
        sceneSuite.genView(),
        appCommandCallback
      )
    }
  }
  def run(application: ui.Application, callback: ApplicationCommand => Unit = _ => ()): Unit = {
    val appState = AppState.init(application, callback)
    internalAppState = Some(appState)
    SceneHandler.commandHandler(appState, SceneHandler.Initialize)
  }
  def postCommand(command: Scene#Command): Unit = try {
    internalAppState = internalAppState.map { appState =>
      handleCommand(
        appState,
        ViewCallbackWrap(ExecutionCallback(Execution(command)))
      )
    }
  } catch {
    case e: ClassCastException =>
  }
  def handleCommand(appState: AppState, command: ApplicationCommand): AppState = {
    internalAppState = Some(appState)
    appState.appCommandCallback(command)
    command match {
      case SceneCallbackWrap(sceneCallback) => handleSceneCallback(appState, sceneCallback)
      case PresenterCallbackWrap(presenterCallback) => handlePresenterCallback(appState, presenterCallback)
      case ViewCallbackWrap(viewCallback) => handleViewCallback(appState, viewCallback)
    }
  }
  def handleSceneCallback(appState: AppState, sceneCallback: SceneCallback): AppState = sceneCallback match {
    case ui.Initialized(initialState) =>
      PresenterHandler.commandHandler(
        appState, PresenterHandler.Initialize(initialState, appState.currentSceneSuite.genPresenter))
    case ui.StateChangedCallback(state) =>
      PresenterHandler.commandHandler(appState, PresenterHandler.Update(state))
    case ui.EventCallback(event) => ???
    case ui.NextSceneCallback(sceneKey) =>
      SceneHandler.commandHandler(appState.nextScene(sceneKey), SceneHandler.CleanUp)
    case ui.CleanedUp =>
      PresenterHandler.commandHandler(appState, PresenterHandler.Cleanup)
    case ui.NoCallback => appState
  }
  def handlePresenterCallback(appState: AppState, presenterCallback: PresenterCallback): AppState = presenterCallback match {
    case PresenterHandler.Initialized(viewModel) =>
      ViewHandler.commandHandler(appState, ViewHandler.Initialize(appState.currentSceneSuite.genView, viewModel))
    case PresenterHandler.Updated(viewModelMod) =>
      ViewHandler.commandHandler(appState, ViewHandler.Update(viewModelMod.asInstanceOf[ViewModel => ViewModel]))
    case PresenterHandler.CleanedUp =>
      ViewHandler.commandHandler(appState, ViewHandler.CleanUp)
  }
  def handleViewCallback(appState: AppState, viewCallback: ViewCallback): AppState = viewCallback match {
    case ViewHandler.Initialized =>
      SceneHandler.commandHandler(appState, SceneHandler.Start)
    case ViewHandler.ExecutionCallback(cmd) =>
      SceneHandler.commandHandler(appState, cmd)
    case ViewHandler.CleanedUp =>
      val newScene = appState.currentSceneSuite.genScene()
      SceneHandler.commandHandler(
        appState.copy(
          scene = newScene,
          state = newScene.initialState(),
          presenter = appState.currentSceneSuite.genPresenter(),
          view = appState.currentSceneSuite.genView()
        ),
        SceneHandler.Initialize
      )
  }
}

object SceneHandler {
  type GenScene = () => ui.Scene
  sealed trait SceneCommand[-T]
  case object Initialize extends SceneCommand[Nothing]
  case object CleanUp extends SceneCommand[Nothing]
  case object Start extends SceneCommand[Nothing]
  case object Stop extends SceneCommand[Nothing]
  final case class Execution[S <: ui.Scene](input: S#Command) extends SceneCommand[S]

  def commandHandler(appState: AppState, command: SceneCommand[_]): AppState = command match {
    case Initialize =>
      val state = appState.scene.initialState()
      ApplicationHandler.handleCommand(appState, SceneCallbackWrap(Initialized(state)))
    case Execution(command) =>
      import appState.{scene, state}
      scene.execute(state.asInstanceOf[scene.State], command.asInstanceOf[scene.Command]) match {
        case (s, _, cb) =>
          val newAppState =
            if (s != state) {
              ApplicationHandler.handleCommand(
                appState.copy(state = s),
                SceneCallbackWrap(StateChangedCallback(s))
              )
            } else appState
          ApplicationHandler.handleCommand(newAppState, SceneCallbackWrap(cb))
      }
    case CleanUp =>
      appState.scene.cleanup()
      ApplicationHandler.handleCommand(appState, SceneCallbackWrap(CleanedUp))
    case _ => appState
  }
}

object PresenterHandler {
  type GenPresenter[M <: ViewModel] = () => ui.Presenter
  sealed trait PresenterCommand
  final case class Initialize[S <: ui.Scene, M <: ViewModel](initialState: S#State, genPresenter: GenPresenter[M])
    extends PresenterCommand
  final case class Update[S <: ui.Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ViewModel) extends PresenterCallback
  case object CleanedUp extends PresenterCallback
  final case class Updated[M <: ViewModel](viewModelMod: M => M) extends PresenterCallback

  def commandHandler(appState: AppState, command: PresenterCommand): AppState = command match {
    case Initialize(state, _) =>
      val viewModel = appState.presenter.setup(state.asInstanceOf[appState.presenter.S#State])
      ApplicationHandler.handleCommand(appState, PresenterCallbackWrap(Initialized(viewModel)))
    case Update(state) =>
      val viewModelMod = appState.presenter.updated(state.asInstanceOf[appState.presenter.S#State])
      ApplicationHandler.handleCommand(appState, PresenterCallbackWrap(Updated(viewModelMod)))
    case Cleanup =>
      appState.presenter.cleanup()
      ApplicationHandler.handleCommand(appState, PresenterCallbackWrap(CleanedUp))
    case _ => ???
  }
}

object ViewHandler {
  type GenView = () => ui.View
  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ViewModel) extends ViewCommand
  final case class Update(viewModelMod: ViewModel => ViewModel) extends ViewCommand
  case object CleanUp extends ViewCommand

  sealed trait ViewCallback
  case object Initialized extends ViewCallback
  case object CleanedUp extends ViewCallback
  final case class ExecutionCallback(command: SceneCommand[_]) extends ViewCallback

  def commandHandler(appState: AppState, command: ViewCommand): AppState = command match {
    case Initialize(_, viewModel) =>
      import appState.view
      view.setup(viewModel.asInstanceOf[view.M],
        msg => ApplicationHandler.postCommand(msg))
      ApplicationHandler.handleCommand(appState, ViewCallbackWrap(Initialized))
    case Update(viewModelMod) =>
      appState.view.update(viewModelMod.asInstanceOf[appState.view.M => appState.view.M])
      appState
    case CleanUp =>
      appState.view.cleanup()
      ApplicationHandler.handleCommand(appState, ViewCallbackWrap(CleanedUp))
    case _ => ???
  }
}
