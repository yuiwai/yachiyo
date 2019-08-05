package com.yuiwai.yachiyo.akka

import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Scene.{Execution, SceneCommand}
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.ViewModel

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

  def deployed(view: ui.View, initialViewModel: ViewModel, listener: ViewCallback => Unit): Behaviors.Receive[ViewCommand] = {
    // FIXME 暫定
    view.setup(initialViewModel.asInstanceOf[view.M], c => listener(ExecutionCallback(Execution(c))))

    Behaviors.receiveMessage[ViewCommand] {
      case Update(viewModel) =>
        view.update(viewModel.asInstanceOf[view.M])
        Behaviors.same
      case CleanUp =>
        view.cleanup()
        listener(CleanedUp)
        init(listener)
      case _ => Behaviors.same
    }
  }

  def init(listener: ViewCallback => Unit): Behaviors.Receive[ViewCommand] = {
    Behaviors.receive[ViewCommand] { (_, msg) =>
      msg match {
        // TODO 初期化後のステートへ遷移
        case Initialize(genView, viewModel) =>
          listener(Initialized)
          deployed(genView(), viewModel, listener)
        case _ => Behaviors.same
      }
    }
  }
}

