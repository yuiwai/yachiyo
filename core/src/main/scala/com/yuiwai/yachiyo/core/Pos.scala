package com.yuiwai.yachiyo.core

final case class Pos[T: Amount](x: T, y: T) extends Vector2D[T] {
  def +(that: Vector2D[T]): Pos[T] = copy(amount.+(x, that.x), amount.+(y, that.y))
  def modX(f: T => T): Pos[T] = copy(x = f(x))
  def modY(f: T => T): Pos[T] = copy(y = f(y))
}
object Pos {
  def zero[T](implicit a: Amount[T]): Pos[T] = apply(a.zero, a.zero)
}
