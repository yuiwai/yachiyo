package com.yuiwai.yachiyo.akka

import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Scene.{Execution, SceneCommand}
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.ViewModel

object View {
  type GenView = () => ui.View
  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ViewModel) extends ViewCommand
  // TODO draw, cleanupもコマンド経由で

  sealed trait ViewCallback
  final case class ExecutionCallback(command: SceneCommand[_]) extends ViewCallback

  def deployed(view: ui.View, viewModel: ViewModel, listener: ViewCallback => Unit): Behaviors.Receive[ViewCommand] = {
    // FIXME 暫定
    view.setup(viewModel.asInstanceOf[view.M], c => listener(ExecutionCallback(Execution(c))))

    Behaviors.receiveMessage[ViewCommand] { msg =>
      // TODO コマンド処理
      Behaviors.same
    }
  }

  def init(listener: ViewCallback => Unit): Behaviors.Receive[ViewCommand] = {
    Behaviors.receive[ViewCommand] { (_, msg) =>
      msg match {
        // TODO 初期化後のステートへ遷移
        case Initialize(genView, viewModel) =>
          deployed(genView(), viewModel, listener)
        case _ => Behaviors.same
      }
    }
  }
}

