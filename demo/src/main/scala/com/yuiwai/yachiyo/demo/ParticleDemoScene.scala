package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core._
import com.yuiwai.yachiyo.demo.ParticleDemoScene.{BackToTop, ParticleDemoState, RequestUpdate}
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLElement}

object ParticleDemoScene extends Scene {

  override type State = ParticleDemoState
  override type Command = ParticleDemoMsg
  override type Event = None.type

  final case class ParticleDemoState(system: ParticleSystem[Double]) {
    def updated(): ParticleDemoState = copy(system = system.updated())
  }
  sealed trait ParticleDemoMsg
  case object BackToTop extends ParticleDemoMsg
  case object RequestUpdate extends ParticleDemoMsg

  private val lifeTime = 500
  private val initialSpeed = Speed(0, -2.0)
  private val initialGravity = Gravity(Force(0, .01))
  private val generatorPos = Pos(150.0, 150.0)
  override def initialState(): ParticleDemoState = ParticleDemoState(ParticleSystem[Double](
    generatorPos,
    lifeTime,
    Seq.empty,
    Generator(s => s.spawn(initialSpeed * Angle.random(-45, 45))),
    initialGravity
  ))
  override def execute(state: ParticleDemoState, input: ParticleDemoMsg): Result = input match {
    case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
    case RequestUpdate => (state.updated(), None, NoCallback)
  }
}

class ParticleDemoPresenter extends Presenter {
  import ParticleDemoViewModel.{circle, field}
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  override def updated(state: ParticleDemoState, prevModel: Prev): ParticleDemoViewModel = {
    ParticleDemoViewModel(state.system.particles.foldLeft(field) { case (b, p) =>
      b.patch(Pos(p.pos.x - circle.width / 2, p.pos.y - circle.height / 2).map(_.toInt), circle)
    }, state.system.particles.size)
  }
}

final case class ParticleDemoViewModel(
  block: Block[Boolean],
  particleCount: Int
) extends ViewModel
object ParticleDemoViewModel {
  val canvasWidth = 300
  val canvasHeight = 300
  val circle: Block[Boolean] = Drawing.circle(2)
  val field: Block[Boolean] = Block.fillZero[Boolean](canvasWidth, canvasHeight)
}

class ParticleDemoView extends CanvasView with CommonView {
  import ParticleDemoViewModel.{canvasHeight, canvasWidth}
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  private var playing = true
  private var ctx2d: Option[CanvasRenderingContext2D] = None
  private var counter: Option[HTMLElement] = None
  override def setup(viewModel: ParticleDemoViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(div(btn))

    val canvas = createCanvas(canvasWidth, canvasHeight)
    ctx2d = Some(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    container.appendChild(canvas)

    counter = Some(div().tap(e => container.appendChild(e)))

    loop(listener)
  }
  override def update(viewModel: ParticleDemoViewModel): Unit = {
    counter.foreach(_.innerHTML = viewModel.particleCount.toString)
    draw(viewModel.block)
  }
  override def cleanup(): Unit = {
    super.cleanup()
    playing = false
  }
  private def draw(block: Block[Boolean]): Unit = {
    ctx2d.foreach { ctx =>
      ctx.beginPath()
      ctx.fillStyle = "rgba(0, 0, 0, .05)"
      ctx.fillRect(0, 0, canvasWidth, canvasHeight)
      val imageData = ctx.getImageData(0, 0, canvasWidth, canvasHeight)
      var i = 0
      block.values.foreach { v =>
        if (v) {
          imageData.data(i) = 255
          imageData.data(i + 1) = 255
          imageData.data(i + 2) = 0
          imageData.data(i + 3) = 255
        }
        i += 4
      }
      ctx.putImageData(imageData, 0, 0, 0, 0, canvasWidth, canvasHeight)
    }
  }
  private def loop(implicit listener: Listener): Unit = {
    if (playing) {
      dom.window.requestAnimationFrame(_ => loop)
      listener(RequestUpdate)
    }
  }
}
