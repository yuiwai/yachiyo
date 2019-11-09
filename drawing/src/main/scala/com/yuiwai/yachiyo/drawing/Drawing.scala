package com.yuiwai.yachiyo.drawing

import com.yuiwai.yachiyo.core.{Color, ToDouble}

trait DrawingOps[T, C] {
  def drawLine(fromX: T, fromY: T, toX: T, toY: T): DrawAction[T] = DrawLineAction(fromX, fromY, toX, toY)
  def drawCircle(x: T, y: T, radius: T): DrawAction[T] = DrawCircleAction(x, y, radius)
  def drawRect(x: T, y: T, width: T, height: T): DrawAction[T] = DrawRectAction(x, y, width, height)
  def fill(color: Color): DrawAction[T]
  def stroke(color: Color): DrawAction[T]
  def execute(action: DrawAction[T])(implicit ctx: C, d: ToDouble[T]): Unit
  def execute(actions: Seq[DrawAction[T]])(implicit ctx: C, d: ToDouble[T]): Unit =
    actions foreach execute
}

final case class LineStyle[T](lineColor: Color, lineWidth: T)
final case class FillStyle(fillColor: Color)

sealed trait DrawAction[T]
final case class DrawLineAction[T](fromX: T, fromY: T, toX: T, toY: T) extends DrawAction[T]
final case class DrawCircleAction[T](x: T, y: T, radius: T) extends DrawAction[T]
final case class DrawRectAction[T](x: T, y: T, width: T, height: T) extends DrawAction[T]
