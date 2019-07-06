package com.yuiwai.yachiyo.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Scene.{Execution, SceneCommand}

trait View {
  type M <: ViewModel
  type S <: Scene
  type Listener = S#Command => Unit
  def setup(viewModel: M, listener: Listener): Unit
  def cleanup(): Unit
  def draw(viewModel: M): Unit
}
object View {
  type GenView = () => View
  sealed trait ViewCommand
  final case class Initialize(genView: GenView, viewModel: ViewModel, sceneRef: ActorRef[SceneCommand[_]]) extends ViewCommand
  // TODO draw, cleanupもコマンド経由で

  def deployed(view: View, viewModel: ViewModel, sceneRef: ActorRef[SceneCommand[_]]): Behaviors.Receive[ViewCommand] = {
    // FIXME 暫定
    view.setup(viewModel.asInstanceOf[view.M], c => sceneRef ! Execution(c))
    Behaviors.receiveMessage[ViewCommand] { msg =>
      Behaviors.same
    }
  }

  def init(): Behaviors.Receive[ViewCommand] = {
    Behaviors.receive[ViewCommand] { (_, msg) =>
      msg match {
        // TODO 初期化後のステートへ遷移
        case Initialize(genView, viewModel, sceneRef) =>
          deployed(genView(), viewModel, sceneRef)
        case _ => Behaviors.same
      }
    }
  }
}

trait ViewModel

