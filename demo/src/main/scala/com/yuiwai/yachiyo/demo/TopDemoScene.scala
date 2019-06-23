package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.akka.Scene.{NextSceneCallback, NoCallback, SceneCallback}
import com.yuiwai.yachiyo.akka.{DomView, Presenter, Scene, ViewModel}

object TopDemoScene extends Scene {
  override type State = None.type
  override type Command = Int
  override type Event = None.type
  val ToTransitionDemo = 1

  override def initialState(): None.type = None
  override def presenter(): Presenter[TopDemoScene.this.type] = new TopPresenter
  override def execute(state: None.type, input: Int): (None.type, Event, SceneCallback) = input match {
    case ToTransitionDemo => (None, None, NextSceneCallback(() => TransitionDemoScene))
    case _ => (None, None, NoCallback)
  }
  override def cleanup(): Unit = {}
}

class TopPresenter extends Presenter[TopDemoScene.type] {
  private val view = new TopView
  private var viewModel: Option[TopViewModel] = None
  def setup(initialState: None.type, listener: Listener): Unit = {
    viewModel = Some(TopViewModel(_ => listener(1)))
    view.setup(viewModel.get)
  }
  def cleanup(): Unit = {
    view.cleanup()
  }
  def updated(state: None.type): Unit = {}
}

case class TopViewModel(
  toTransitionDemo: Unit => Unit
) extends ViewModel

class TopView extends DomView[TopViewModel] {
  private def container = elementById("container")
  override def setup(viewModel: TopViewModel): Unit = {
    val btn = button("Transition Demo")
    btn.onclick = { _ => viewModel.toTransitionDemo() }
    container.appendChild(btn)
  }
  override def cleanup(): Unit = {
    container.innerHTML = ""
  }
  override def draw(viewModel: TopViewModel): Unit = ???
}
