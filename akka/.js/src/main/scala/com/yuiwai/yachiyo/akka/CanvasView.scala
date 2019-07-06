package com.yuiwai.yachiyo.akka

import org.scalajs.dom.raw.HTMLCanvasElement

trait CanvasView extends DomView {
  def createCanvas(width: Int, height: Int): HTMLCanvasElement = {
    createElementAs[HTMLCanvasElement]("canvas", { e =>
      e.width = width
      e.height = height
    })
  }
}

