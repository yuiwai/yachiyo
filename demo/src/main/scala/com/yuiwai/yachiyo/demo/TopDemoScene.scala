package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.demo.TopDemoScene.{ToNodeDemo, ToParticleDemo, ToTransitionDemo}
import com.yuiwai.yachiyo.ui._

import scala.None

object TopDemoScene extends Scene {
  override type State = None.type
  override type Command = TopDemoCommand
  override type Event = None.type

  sealed trait TopDemoCommand
  case object ToTransitionDemo extends TopDemoCommand
  case object ToParticleDemo extends TopDemoCommand
  case object ToNodeDemo extends TopDemoCommand

  override def initialState(): None.type = None
  override def execute(state: None.type, input: TopDemoCommand): (None.type, Event, SceneCallback) = input match {
    case ToTransitionDemo => (None, None, NextSceneCallback(DemoApplication.TransitionSceneKey))
    case ToParticleDemo => (None, None, NextSceneCallback(DemoApplication.ParticleSceneKey))
    case ToNodeDemo => (None, None, NextSceneCallback(DemoApplication.NodeSceneKey))
    case _ => (None, None, NoCallback)
  }
  override def cleanup(): Unit = {}
}

class TopPresenter extends Presenter {
  override type S = TopDemoScene.type
  override type M = TopViewModel
  override def updated(state: None.type, prevModel: Prev): TopViewModel = TopViewModel()
}

case class TopViewModel() extends ViewModel

class TopView extends DomView {
  override type S = TopDemoScene.type
  override type M = TopViewModel
  private def container = elementById("container")
  override def setup(viewModel: M, listener: Listener): Unit = {
    Seq(
      "Transition Demo" -> ToTransitionDemo,
      "Particle Demo" -> ToParticleDemo,
      "Node Demo" -> ToNodeDemo,
    ) foreach {
      case (label, command) =>
        val btn = button(label)
        btn.onclick = _ => listener(command)
        container.appendChild(btn)
    }
  }
  override def cleanup(): Unit = {
    container.innerHTML = ""
  }
  override def update(viewModel: TopViewModel): Unit = {}
}
