package com.yuiwai.yachiyo.drawing.js.canvas

import com.yuiwai.yachiyo.core.{Color, RGB, ToDouble}
import com.yuiwai.yachiyo.drawing.{DrawAction, DrawCircleAction, FillStyleAction, DrawLineAction, DrawRectAction, StrokeStyleAction, DrawTextAction, DrawingOps}
import org.scalajs.dom.raw.CanvasRenderingContext2D

trait Drawing[T] extends DrawingOps[T, CanvasRenderingContext2D] {
  override def execute(action: DrawAction[T])
    (implicit ctx: CanvasRenderingContext2D, d: ToDouble[T]): Unit =
    action match {
      case DrawLineAction(fromX, fromY, toX, toY) =>
        ctx.moveTo(d(fromX), d(fromY))
        ctx.lineTo(d(toX), d(toY))
      case DrawCircleAction(x, y, radius) => {
        ctx.moveTo(d(x), d(y))
        ctx.arc(d(x), d(y), d(radius), 0, Math.PI * 2)
        ctx.closePath()
      }
      case DrawRectAction(x, y, width, height) =>
        ctx.fillRect(d(x), d(y), d(width), d(height))
      case DrawTextAction(text, x, y) => ctx.fillText(text, d(x), d(y))
      case FillStyleAction(color) =>
        ctx.fillStyle = color.toHexString
        ctx.fill()
      case StrokeStyleAction(color) =>
        ctx.strokeStyle = color.toHexString
        ctx.stroke()
    }

  implicit class ColorWrap(color: Color) {
    def toHexString: String = color match {
      case rgb: RGB => "#" + rgb.toHexString
    }
  }
}
