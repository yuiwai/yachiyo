package com.yuiwai.yachiyo.ui

import org.scalajs.dom.raw.{WebGLProgram, WebGLShader, WebGLRenderingContext => GL}

trait WebGLView extends CanvasView {
  def createVertexShader(source: String)(implicit gl: GL): WebGLShader = createShader(source, GL.VERTEX_SHADER)
  def createFragmentShader(source: String)(implicit gl: GL): WebGLShader = createShader(source, GL.FRAGMENT_SHADER)
  private def createShader(source: String, shaderType: Int)(implicit gl: GL): WebGLShader = {
    val shader = gl.createShader(shaderType)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    shader
  }
  def createProgram(vs: WebGLShader, fs: WebGLShader)(implicit gl: GL): WebGLProgram = {
    val program = gl.createProgram()
    gl.attachShader(program, vs)
    gl.attachShader(program, fs)
    gl.linkProgram(program)
    program
  }
}
