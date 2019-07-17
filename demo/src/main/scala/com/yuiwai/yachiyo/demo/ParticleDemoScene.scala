package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core._
import com.yuiwai.yachiyo.demo.ParticleDemoScene.{BackToTop, ParticleDemoState, RequestUpdate, UseBlur}
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLInputElement, ImageData}

object ParticleDemoScene extends Scene {

  override type State = ParticleDemoState
  override type Command = ParticleDemoMsg
  override type Event = None.type

  final case class ParticleDemoState(system: ParticleSystem[Double], useBlur: Boolean) {
    def updated(): ParticleDemoState = copy(system = system.updated())
  }
  sealed trait ParticleDemoMsg
  case object BackToTop extends ParticleDemoMsg
  case object RequestUpdate extends ParticleDemoMsg
  final case class UseBlur(flag: Boolean) extends ParticleDemoMsg

  private val lifeTime = 2000
  private val initialSpeed = Speed(0, -2.0)
  private val initialGravity = Gravity(Force(0, .01))
  private val generatorPos = Pos(150.0, 150.0)
  override def initialState(): ParticleDemoState = ParticleDemoState(ParticleSystem[Double](
    generatorPos,
    lifeTime,
    Seq.empty,
    Generator(s => s.spawn(initialSpeed * Angle.random(-45, 45))),
    initialGravity
  ), useBlur = false)
  override def execute(state: ParticleDemoState, input: ParticleDemoMsg): Result = input match {
    case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
    case RequestUpdate => (state.updated(), None, NoCallback)
    case UseBlur(flag) => (state.copy(useBlur = flag), None, NoCallback)
  }
}

class ParticleDemoPresenter extends Presenter {
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  override def usePrevModel: Boolean = true
  override def updated(state: ParticleDemoState, prevModel: Prev): ParticleDemoViewModel = {
    val additional: Seq[ParticleViewModel] = prevModel.map(_.particles.collect {
      case p if p.alpha > 0 => p.copy(alpha = p.alpha - 0.1)
    }).getOrElse(Seq.empty)
    ParticleDemoViewModel(
      useBlur = state.useBlur,
      state.system.particles.map(ParticleViewModel(_)) ++ additional
    )
  }
}

final case class ParticleViewModel(x: Double, y: Double, alpha: Double)
object ParticleViewModel {
  def apply(p: Particle[Double]): ParticleViewModel = apply(p.pos.x, p.pos.y, 1.0)
}
final case class ParticleDemoViewModel(
  useBlur: Boolean,
  particles: Seq[ParticleViewModel]
) extends ViewModel

class ParticleDemoView extends CanvasView with CommonView {
  override type S = ParticleDemoScene.type
  override type M = ParticleDemoViewModel
  val canvasWidth = 300
  val canvasHeight = 300
  private var prev: Option[ImageData] = None
  private var playing = true
  private var ctx2d: Option[CanvasRenderingContext2D] = None
  override def setup(viewModel: ParticleDemoViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(div(btn))

    val canvas = createCanvas(canvasWidth, canvasHeight)
    ctx2d = Some(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    container.appendChild(canvas)

    container.appendChild(createElementAs[HTMLInputElement]("input", { e =>
      e.`type` = "checkbox"
      e.onclick = { _ => listener(UseBlur(e.checked)) }
    }))

    loop(listener)
  }
  override def update(viewModel: ParticleDemoViewModel): Unit = {
    animation(viewModel.particles, viewModel.useBlur)
  }
  override def cleanup(): Unit = {
    super.cleanup()
    prev = None
    playing = false
  }
  private def animation(particles: Seq[ParticleViewModel], useBlur: Boolean): Unit = {
    ctx2d.foreach { ctx =>
      ctx.beginPath()
      ctx.fillStyle = "black"
      ctx.fillRect(0, 0, canvasWidth, canvasHeight)
      particles.foreach { p =>
        ctx.beginPath()
        ctx.fillStyle = s"rgba(255,255,255,${p.alpha})"
        ctx.arc(p.x, p.y, 5, 0, Math.PI * 2, anticlockwise = false)
        ctx.fill()
      }

      if (useBlur) {
        import com.yuiwai.yachiyo.ui.CanvasView.ImageDataWrap
        val data = ctx.getImageData(0, 0, canvasWidth, canvasHeight)
        prev.foreach { pre =>
          data.pixels
            .foreach { p =>
              p.get(pre).foreach(c => p.mod(data, _.mix(c, 1.3)))
            }
          ctx.putImageData(data, 0, 0, 0, 0, canvasWidth, canvasHeight)
        }
        prev = Some(ctx.getImageData(0, 0, canvasWidth, canvasHeight))
      }
    }
  }
  private def loop(implicit listener: Listener): Unit = {
    if (playing) {
      dom.window.requestAnimationFrame(_ => loop)
      listener(RequestUpdate)
    }
  }
}
