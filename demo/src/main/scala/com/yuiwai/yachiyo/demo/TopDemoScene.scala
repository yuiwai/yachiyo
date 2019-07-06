package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.akka.DomView
import com.yuiwai.yachiyo.demo.TopDemoScene.ToTransitionDemo
import com.yuiwai.yachiyo.ui._

object TopDemoScene extends Scene {
  override type State = None.type
  override type Command = TopDemoCommand
  override type Event = None.type

  sealed trait TopDemoCommand
  case object ToTransitionDemo extends TopDemoCommand

  override def initialState(): None.type = None
  override def execute(state: None.type, input: TopDemoCommand): (None.type, Event, SceneCallback) = input match {
    case ToTransitionDemo => (None, None, NextSceneCallback(() => TransitionDemoScene))
    case _ => (None, None, NoCallback)
  }
  override def cleanup(): Unit = {}
}

class TopPresenter extends Presenter {
  override type S = TopDemoScene.type
  override type M = TopViewModel
  override def updated(state: None.type): TopViewModel = TopViewModel()
}

case class TopViewModel() extends ViewModel

class TopView extends DomView {
  override type S = TopDemoScene.type
  override type M = TopViewModel
  private def container = elementById("container")
  override def setup(viewModel: M, listener: Listener): Unit = {
    val btn = button("Transition Demo")
    btn.onclick = _ => listener(ToTransitionDemo)
    container.appendChild(btn)
  }
  override def cleanup(): Unit = {
    container.innerHTML = ""
  }
  override def draw(viewModel: TopViewModel): Unit = ???
}
