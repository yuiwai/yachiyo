package com.yuiwai.yachiyo.drawing.js.canvas

import com.yuiwai.yachiyo.core.{Color, RGB, ToDouble}
import com.yuiwai.yachiyo.drawing._
import org.scalajs.dom.raw.CanvasRenderingContext2D

trait Drawing[T] extends DrawingOps[T, CanvasRenderingContext2D] {
  override def execute(action: DrawingAction[T])
    (implicit ctx: CanvasRenderingContext2D, d: ToDouble[T]): Unit =
    action match {
      case StrokeLineAction(fromX, fromY, toX, toY) =>
        ctx.beginPath()
        ctx.moveTo(d(fromX), d(fromY))
        ctx.lineTo(d(toX), d(toY))
        ctx.closePath()
        ctx.stroke()
      case FillCircleAction(x, y, radius) => {
        ctx.beginPath()
        ctx.moveTo(d(x), d(y))
        ctx.arc(d(x), d(y), d(radius), 0, Math.PI * 2)
        ctx.closePath()
        ctx.fill()
      }
      case FillRectAction(x, y, width, height) =>
        ctx.fillRect(d(x), d(y), d(width), d(height))
      case FillTextAction(text, x, y) => ctx.fillText(text, d(x), d(y))
      case FillStyleAction(color) =>
        ctx.fillStyle = color.toHexString
      case StrokeStyleAction(color) =>
        ctx.strokeStyle = color.toHexString
      case FontStyleAction(fontSize) =>
        ctx.font = s"${fontSize}px serif" // TODO FontStyle対応
      case CompositeDrawingAction(actions) => actions.foreach(execute)
    }

  implicit class ColorWrap(color: Color) {
    def toHexString: String = color match {
      case rgb: RGB => "#" + rgb.toHexString
    }
  }
}
