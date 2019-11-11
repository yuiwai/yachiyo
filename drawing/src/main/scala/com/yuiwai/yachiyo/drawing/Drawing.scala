package com.yuiwai.yachiyo.drawing

import com.yuiwai.yachiyo.core.{Color, ToDouble}

trait DrawingOps[T, C] {
  def strokeLine(fromX: T, fromY: T, toX: T, toY: T): DrawingAction[T] =StrokeLineAction(fromX, fromY, toX, toY)
  @deprecated("use strokeLine")
  def drawLine(fromX: T, fromY: T, toX: T, toY: T): DrawingAction[T] = strokeLine(fromX, fromY, toX, toY)
  def fillCircle(x: T, y: T, radius: T): DrawingAction[T] = FillCircleAction(x, y, radius)
  @deprecated("use fillCircle")
  def drawCircle(x: T, y: T, radius: T): DrawingAction[T] = fillCircle(x, y, radius)
  def fillRect(x: T, y: T, width: T, height: T): DrawingAction[T] = FillRectAction(x, y, width, height)
  @deprecated("use fillRect")
  def drawRect(x: T, y: T, width: T, height: T): DrawingAction[T] = fillRect(x, y, width, height)
  def fillText(text: String, x: T, y: T): DrawingAction[T] = FillTextAction(text, x, y)
  @deprecated("use fillText")
  def drawText(text: String, x: T, y: T): DrawingAction[T] = fillText(text, x, y)
  def fillStyle(color: Color): DrawingAction[T] = FillStyleAction(color)
  def strokeStyle(color: Color): DrawingAction[T] = StrokeStyleAction(color)
  def fontStyle(fontSize: Int): DrawingAction[T] = FontStyleAction(fontSize)
  def execute(action: DrawingAction[T])(implicit ctx: C, d: ToDouble[T]): Unit
  def execute(actions: Seq[DrawingAction[T]])(implicit ctx: C, d: ToDouble[T]): Unit =
    actions foreach execute
}

final case class StrokeStyle[T](strokeColor: Color, strokeWidth: T)
final case class FillStyle(fillColor: Color)
final case class FontStyle(fontSize: Int, fontFamily: String) // TODO FontSizeを抽象化した方がいい

sealed trait DrawingAction[T]
final case class StrokeLineAction[T](fromX: T, fromY: T, toX: T, toY: T) extends DrawingAction[T]
final case class FillCircleAction[T](x: T, y: T, radius: T) extends DrawingAction[T]
final case class FillRectAction[T](x: T, y: T, width: T, height: T) extends DrawingAction[T]
final case class FillTextAction[T](text: String, x: T, y: T) extends DrawingAction[T]
// TODO StrokeStyleを使いたい
final case class StrokeStyleAction[T](color: Color) extends DrawingAction[T]
// TODO FillStyleを使いたい
final case class FillStyleAction[T](color: Color) extends DrawingAction[T]
// TODO FontStyleを使いたい
final case class FontStyleAction[T](i: Int) extends DrawingAction[T]
final case class CompositeDrawingAction[T](actions: Seq[DrawingAction[T]]) extends DrawingAction[T]

