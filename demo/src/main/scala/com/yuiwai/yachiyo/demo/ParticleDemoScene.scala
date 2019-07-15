package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core._
import com.yuiwai.yachiyo.demo.ParticleDemoScene.BackToTop
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom
import org.scalajs.dom.raw.CanvasRenderingContext2D

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
  val canvasWidth = 300
  val canvasHeight = 300
  val lifeTime = 2000
  val initialSpeed = Speed(0, -2.0)
  val initialGravity = Gravity(Force(0, .01))
  private var system = ParticleSystem[Double](
    Pos(canvasWidth / 2, canvasHeight / 2),
    lifeTime,
    Seq.empty,
    Generator(s => s.spawn(initialSpeed * Angle.random(-45, 45))),
    initialGravity
  )
  private var playing = true
  override def setup(viewModel: ParticleDemoViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(div(btn))

    val canvas = createCanvas(canvasWidth, canvasHeight)
    container.appendChild(canvas)

    animation(0)(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
  }
  override def update(viewModel: ParticleDemoViewModel): Unit = {}
  override def cleanup(): Unit = {
    super.cleanup()
    playing = false
  }
  private def animation(time: Double)(implicit ctx: CanvasRenderingContext2D): Unit = {
    system = system.updated()
    ctx.beginPath()
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, canvasWidth, canvasHeight)
    system.particles.foreach { p =>
      import p.pos
      ctx.beginPath()
      ctx.fillStyle = "rgba(255,255,255,.5)"
      ctx.arc(pos.x, pos.y, 5, 0, Math.PI * 2, false)
      ctx.fill()
    }
    if (playing) dom.window.requestAnimationFrame(animation(_))
  }
}
