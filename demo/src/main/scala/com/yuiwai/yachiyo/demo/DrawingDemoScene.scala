package com.yuiwai.yachiyo.demo

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
  private var ctx2d: Option[CanvasRenderingContext2D] = None
  override def setup(viewModel: DrawingDemoViewModel, listener: Listener): Unit = {
    val canvas = createCanvas(canvasWidth, canvasHeight)
    ctx2d = Some(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    container.appendChild(canvas)
  }
  override def update(viewModel: DrawingDemoViewModel): Unit = {

  }
  override def cleanup(): Unit = {
    super.cleanup()
  }
}
