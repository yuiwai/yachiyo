package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.core._
import com.yuiwai.yachiyo.demo.WebGLDemoScene.BackToTop
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom
import org.scalajs.dom.raw.{WebGLRenderingContext => GL}

object WebGLDemoScene extends Scene {
  override type State = None.type
  override type Command = WebGLDemoMsg
  override type Event = None.type
  sealed trait WebGLDemoMsg
  case object BackToTop extends WebGLDemoMsg

  override def initialState(): None.type = None
  override def execute(state: None.type, input: WebGLDemoMsg): Result = input match {
    case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
  }
  override def cleanup(): Unit = {}
}

class WebGLDemoPresenter extends Presenter {
  override type S = WebGLDemoScene.type
  override type M = WebGLDemoViewModel
  override def updated(state: None.type): WebGLDemoViewModel = WebGLDemoViewModel()
}

final case class WebGLDemoViewModel() extends ViewModel

class WebGLDemoView extends WebGLView with CommonView {
  override type S = WebGLDemoScene.type
  override type M = WebGLDemoViewModel
  val canvasWidth = 300
  val canvasHeight = 300
  val lifeTime = 2000
  val initialSpeed = Speed(0, -2.0)
  val initialGravity = Gravity(Force(0, .01))
  private var playing = false
  override def setup(viewModel: WebGLDemoViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(div(btn))

    val canvas = createCanvas(canvasWidth, canvasHeight)
    container.appendChild(canvas)

    implicit val gl: GL = canvas.getContext("webgl").asInstanceOf[GL]
    gl.clearColor(0.0, 0.0, 0.0, 1.0)
    gl.clear(GL.COLOR_BUFFER_BIT)

    val program = createProgram()
    val position = gl.getAttribLocation(program, "position")
    val vertex = Seq(
      0.0f, 1.0f, 0.0f,
      1.0f, 0.0f, 0.0f,
      -1.0f, 0.0f, 0.0f
    )
    val vbo = createVBO(vertex)

    gl.bindBuffer(GL.ARRAY_BUFFER, vbo)
    gl.enableVertexAttribArray(position)
    gl.vertexAttribPointer(position, 3, GL.FLOAT, false, 0, 0)
    gl.drawArrays(GL.TRIANGLES, 0, 3)
    gl.flush()

    // animation(0)(canvas.getContext("webgl").asInstanceOf[WebGLRenderingContext])
  }
  override def update(viewModel: WebGLDemoViewModel): Unit = {}
  override def cleanup(): Unit = {
    super.cleanup()
    playing = false
  }
  private def animation(time: Double)(implicit gl: GL): Unit = {
    gl.clearColor(0.0, 0.0, 0.0, 1.0)
    gl.clear(GL.COLOR_BUFFER_BIT)

    if (playing) dom.window.requestAnimationFrame(animation(_))
  }
}
