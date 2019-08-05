package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core.{Block, Drawing, Pos, RGBA}
import com.yuiwai.yachiyo.demo.DrawingDemoScene.BackToTop
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom.raw.CanvasRenderingContext2D

object DrawingDemoScene extends Scene {
  override type State = DrawingDemoState
  override type Command = DrawingDemoCommand
  override type Event = None.type

  final case class DrawingDemoState()

  sealed trait DrawingDemoCommand
  case object BackToTop extends DrawingDemoCommand

  override def initialState(): DrawingDemoState = DrawingDemoState()
  override def execute(state: DrawingDemoState, input: DrawingDemoCommand):
  (DrawingDemoState, None.type, SceneCallback) = input match {
    case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
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
  val resizedWidth = 20
  val resizedHeight = 20
  val margin = 10
  private var ctx2d: Option[CanvasRenderingContext2D] = None
  private val circle = Drawing.circle(originRadius).resizeTo(resizedWidth, resizedHeight).get.map { d =>
    if (d > 0) RGBA(0, 0, 255, (255 * d).toInt)
    else RGBA(255, 255, 255, 255)
  }
  private val lines = Seq(
    Drawing.line(Pos(0, 0), Pos(canvasWidth, canvasHeight), 1),
    Drawing.line(Pos(0.0, 0.0), Pos(canvasWidth - 50.0, canvasHeight - 0.0), 1.0),
    Drawing.line(Pos(0.0, 100.0), Pos(canvasWidth, 100.0), 2.0),
    Drawing.line(Pos(100.0, 0.0), Pos(100.0, canvasHeight), 2.0),
  ).map(_.map { d =>
    if (d > 0) RGBA(0, 0, 0, 255) else RGBA(0, 0, 0, 0)
  })
  private var block: Option[Block[RGBA]] = None
  override def setup(viewModel: DrawingDemoViewModel, listener: Listener): Unit = {
    container.appendChild(div(
      button("Back To Top")
        .tap(
          _.onclick = { _ => listener(BackToTop) }
        )
    ))

    val canvas = createCanvas(canvasWidth, canvasHeight)
    ctx2d = Some(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    container.appendChild(canvas)

    block = Some {
      lines.foldLeft(Block.fillTile(canvasWidth, canvasHeight, circle)) {
        (acc, b) => acc.mix(Pos(0, 0), b, (a, b) => a alphaBlend b)
      }
    }
  }
  override def update(viewModel: DrawingDemoViewModel): Unit = {
    ctx2d.foreach { ctx =>
      val imageData = ctx.getImageData(0, 0, canvasWidth, canvasHeight)

      block.foreach { b =>
        var i = 0
        b.values.foreach { rgb =>
          imageData.data(i) = rgb.r
          imageData.data(i + 1) = rgb.g
          imageData.data(i + 2) = rgb.b
          imageData.data(i + 3) = 255
          i += 4
        }
      }

      ctx.putImageData(imageData, 0, 0, 0, 0, canvasWidth, canvasHeight)
    }
  }
  override def cleanup(): Unit = {
    super.cleanup()
  }
}
