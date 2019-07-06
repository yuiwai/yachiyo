package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.akka.CanvasView
import com.yuiwai.yachiyo.core.Particle
import com.yuiwai.yachiyo.demo.ParticleDemoScene.BackToTop
import com.yuiwai.yachiyo.ui._

object ParticleDemoScene extends Scene {
  override type State = Set[Particle[Int]]
  override type Command = ParticleDemoMsg
  override type Event = None.type
  sealed trait ParticleDemoMsg
  case object BackToTop extends ParticleDemoMsg

  override def initialState(): Set[Particle[Int]] = Set.empty
  override def execute(state: Set[Particle[Int]], input: ParticleDemoMsg): Result = input match {
    case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
  }
  override def cleanup(): Unit = {}
}

class ParticleDemoPresenter extends Presenter {
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  override def updated(state: Set[Particle[Int]]): ParticleDemoViewModel = ParticleDemoViewModel()
}

final case class ParticleDemoViewModel() extends ViewModel

class ParticleDemoView extends CanvasView with CommonView {
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  override def setup(viewModel: ParticleDemoViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(div(btn))

    val canvas = createCanvas(500, 500)
    container.appendChild(canvas)
  }
  override def update(viewModel: ParticleDemoViewModel): Unit = {}
}
