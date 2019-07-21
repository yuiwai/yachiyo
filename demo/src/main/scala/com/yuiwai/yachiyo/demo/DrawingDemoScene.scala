package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core.Drawing
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom.raw.CanvasRenderingContext2D

object DrawingDemoScene extends Scene {
  override type State = DrawingDemoState
  override type Command = DrawingDemoCommand
  override type Event = None.type

  final case class DrawingDemoState()
  sealed trait DrawingDemoCommand

  override def initialState(): DrawingDemoState = DrawingDemoState()
  override def execute(state: DrawingDemoState, input: DrawingDemoCommand):
  (DrawingDemoState, None.type, SceneCallback) = input match {
    case _ => (state, None, NoCallback)
  }
}

object DrawingDemoPresenter extends Presenter {
  override type S = DrawingDemoScene.type
  override type M = DrawingDemoViewModel
  override def updated(
    state: DrawingDemoScene.DrawingDemoState,
    prevModel: DrawingDemoPresenter.Prev
  ): DrawingDemoViewModel = {
    DrawingDemoViewModel()
  }
}

final case class DrawingDemoViewModel() extends ViewModel

final class DrawingDemoView extends CanvasView with CommonView {
  override type M = DrawingDemoViewModel
  override type S = DrawingDemoScene.type
  val canvasWidth = 300
  val canvasHeight = 300
  val originRadius = 10
  val resizedWidth = 50
  val resizedHeight = 40
  val margin = 10
  private var ctx2d: Option[CanvasRenderingContext2D] = None
  private val circle = Drawing.circle(originRadius).resizeTo(resizedWidth, resizedHeight).get
  override def setup(viewModel: DrawingDemoViewModel, listener: Listener): Unit = {
    val canvas = createCanvas(canvasWidth, canvasHeight)
    ctx2d = Some(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    container.appendChild(canvas)
  }
  override def update(viewModel: DrawingDemoViewModel): Unit = {
    ctx2d.foreach { ctx =>
      val imageData = ctx.getImageData(0, 0, canvasWidth, canvasHeight)
      for {
        x <- 0.to(canvasWidth, resizedWidth + margin)
        y <- 0.to(canvasHeight, resizedHeight + margin)
      } {
        circle.valuesWithPos.foreach { case (p, b) =>
          val i = (p.x + x + (p.y + y) * canvasWidth) * 4
          if (b) {
            imageData.data(i) = 0
            imageData.data(i + 1) = 0
            imageData.data(i + 2) = 0
            imageData.data(i + 3) = 50
          }
        }
      }
      ctx.putImageData(imageData, 0, 0, 0, 0, canvasWidth, canvasHeight)
    }
  }
  override def cleanup(): Unit = {
    super.cleanup()
  }
}
