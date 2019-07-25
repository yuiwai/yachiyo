package com.yuiwai.yachiyo.zio

import com.yuiwai.yachiyo.ui.{SceneCallback, ViewModel}
import com.yuiwai.yachiyo.zio.Presenter.PresenterCallback
import com.yuiwai.yachiyo.zio.Scene.SceneCommand
import com.yuiwai.yachiyo.zio.View.ViewCallback
import com.yuiwai.yachiyo.ui

object Application {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  final case class ViewCallbackWrap(viewCallback: ViewCallback) extends ApplicationCommand
}

object Scene {
  type GenScene = () => ui.Scene
  sealed trait SceneCommand[-T]
  final case class Initialize(genScene: GenScene) extends SceneCommand[Nothing]
  case object CleanUp extends SceneCommand[Nothing]
  case object Start extends SceneCommand[Nothing]
  case object Stop extends SceneCommand[Nothing]
  final case class Execution[S <: ui.Scene](input: S#Command) extends SceneCommand[S]
}

object Presenter {
  type GenPresenter[M <: ViewModel] = () => ui.Presenter
  sealed trait PresenterCommand
  final case class Initialize[S <: ui.Scene, M <: ViewModel](initialState: S#State, genPresenter: GenPresenter[M]) extends PresenterCommand
  final case class Update[S <: ui.Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ViewModel) extends PresenterCallback
  case object CleanedUp extends PresenterCallback
  final case class Updated[M <: ViewModel](viewModel: ViewModel) extends PresenterCallback
}

object View {
  type GenView = () => ui.View
  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ViewModel) extends ViewCommand
  final case class Update(viewModel: ViewModel) extends ViewCommand

  case object CleanUp extends ViewCommand
  sealed trait ViewCallback
  case object Initialized extends ViewCallback
  case object CleanedUp extends ViewCallback
  final case class ExecutionCallback(command: SceneCommand[_]) extends ViewCallback
}
