package com.yuiwai.yachiyo.core

final case class Pos[T: Amount](x: T, y: T) extends Vector2D[T] {
  def +(that: Vector2D[T])(implicit plus: Plus[T]): Pos[T] = copy(amount.+(x, that.x), amount.+(y, that.y))
  def modX(f: T => T): Pos[T] = copy(x = f(x))
  def modY(f: T => T): Pos[T] = copy(y = f(y))
  def map[U: Amount](f: T => U): Pos[U] = Pos(f(x), f(y))

}
object Pos {
  def zero[T: Zero](implicit a: Amount[T]): Pos[T] = apply(a.zero, a.zero)
  def middle[T: Amount](pos1: Pos[T], pos2: Pos[T])(implicit plus: Plus[T], divide: Divide[T, Int]): Pos[T] =
    Pos(divide(plus(pos1.x, pos2.x), 2), divide(plus(pos1.y, pos2.y), 2))
}
