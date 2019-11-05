package com.yuiwai.yachiyo.drawing

import com.yuiwai.yachiyo.core.Color

trait Drawing[T] {
  def drawLine(fromX: T, fromY: T, toX: T, toY: T): Unit
  def drawCircle(x: T, y: T, radius: T): Unit
  def drawRect(x: T, y: T, width: T, height: T): Unit
  def fill(color: Color): Unit
  def stroke(color: Color): Unit
}
