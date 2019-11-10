package com.yuiwai.yachiyo.drawing.js.canvas

import com.yuiwai.yachiyo.core.{Color, ToDouble}
import com.yuiwai.yachiyo.drawing.{DrawAction, DrawCircleAction, DrawLineAction, DrawRectAction, DrawingOps}
import org.scalajs.dom.raw.CanvasRenderingContext2D

trait Drawing[T] extends DrawingOps[T, CanvasRenderingContext2D] {
  override def fill(color: Color): DrawAction[T] = ???
  override def stroke(color: Color): DrawAction[T] = ???
  override def execute(action: DrawAction[T])
    (implicit ctx: CanvasRenderingContext2D, d: ToDouble[T]): Unit =
    action match {
      case DrawLineAction(fromX, fromY, toX, toY) =>
        ctx.moveTo(d(fromX), d(fromY))
        ctx.lineTo(d(toX), d(toY))
      case DrawCircleAction(x, y, radius) => {
        ctx.moveTo(d(x), d(y))
        ctx.arc(d(x), d(y), d(radius), 0, Math.PI * 2)
      }
      case DrawRectAction(x, y, width, height) => ctx.rect(d(x), d(y), d(width), d(height))
    }
}
