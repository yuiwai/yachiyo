package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.akka.CanvasView
import com.yuiwai.yachiyo.core.Particle
import com.yuiwai.yachiyo.ui.{NoCallback, Presenter, Scene, ViewModel}

object ParticleDemoScene extends Scene {
  override type State = Set[Particle[Int]]
  override type Command = ParticleDemoMsg
  override type Event = None.type
  sealed trait ParticleDemoMsg
  override def initialState(): Set[Particle[Int]] = Set.empty
  override def execute(state: Set[Particle[Int]], input: ParticleDemoMsg): Result = {
    (state, None, NoCallback)
  }
  override def cleanup(): Unit = {}
}

class ParticleDemoPresenter extends Presenter {
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  override def updated(state: Set[Particle[Int]]): ParticleDemoViewModel = ParticleDemoViewModel()
}

final case class ParticleDemoViewModel() extends ViewModel

class ParticleDemoView extends CanvasView {
  override type M = ParticleDemoViewModel
  override def setup(viewModel: ParticleDemoViewModel, listener: Listener): Unit = ???
  override def cleanup(): Unit = {}
  override def draw(viewModel: ParticleDemoViewModel): Unit = {
    println("draw")
  }
}
