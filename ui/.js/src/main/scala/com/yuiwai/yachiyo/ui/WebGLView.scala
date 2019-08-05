package com.yuiwai.yachiyo.ui

import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram, WebGLShader, WebGLRenderingContext => GL}

import scalajs.js.typedarray.floatArray2Float32Array

trait WebGLView extends CanvasView {
  def createVertexShader(source: String)(implicit gl: GL): WebGLShader = createShader(source, GL.VERTEX_SHADER)
  def createVertexShader()(implicit gl: GL): WebGLShader = {
    createVertexShader(
      """
        |attribute vec3 position;
        |
        |void main(void){
        |    gl_Position = vec4(position, 1.0);
        |}
      """.stripMargin)
  }
  def createFragmentShader(source: String)(implicit gl: GL): WebGLShader = createShader(source, GL.FRAGMENT_SHADER)
  def createFragmentShader()(implicit gl: GL): WebGLShader = {
    createFragmentShader(
      """
        |void main(void){
        |    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        |}
      """.stripMargin)
  }
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
    gl.useProgram(program)
    program
  }
  def createProgram()(implicit gl: GL): WebGLProgram = {
    createProgram(createVertexShader(), createFragmentShader())
  }
  def createVBO(data: Seq[Float])(implicit gl: GL): WebGLBuffer = {
    val vbo = gl.createBuffer()
    gl.bindBuffer(GL.ARRAY_BUFFER, vbo)
    gl.bufferData(GL.ARRAY_BUFFER, floatArray2Float32Array(data.toArray), GL.STATIC_DRAW)
    gl.bindBuffer(GL.ARRAY_BUFFER, null)
    vbo
  }
}
