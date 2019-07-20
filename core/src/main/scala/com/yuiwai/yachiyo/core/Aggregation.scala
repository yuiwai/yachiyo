package com.yuiwai.yachiyo.core

import scala.language.higherKinds

trait Aggregation[F[_], T] {
  def aggregate(data: F[T]): T
}
object Aggregation {
  def sum[V: Add](values: Seq[V]): V = values.reduce(implicitly[Add[V]].apply)
  def avg[L: Add, R](values: Seq[L])(implicit divide: Divide[L, Int]): L = divide(sum(values), values.size)
  def mid[V](values: Seq[V]): V = values(values.size / 2)
  def max[V: Ordering](values: Seq[V]): V = values.max
  def min[V: Ordering](values: Seq[V]): V = values.min
}

trait Add[V] {
  def apply(l: V, r: V): V
}
object Add {
  implicit val intAdd: Add[Int] = _ + _
  implicit val doubleAdd: Add[Double] = _ + _
}
trait Divide[L, R] {
  def apply(l: L, r: R): L
}
object Divide {
  type DivideByInt[T] = Divide[T, Int]
  implicit val intDivide: Divide[Int, Int] = _ / _
  implicit val doubleDivide: Divide[Double, Double] = _ / _
  implicit val floatIntDivide: Divide[Float, Int] = _ / _
  implicit val doubleIntDivide: Divide[Double, Int] = _ / _
  implicit def posIntDivide[T: Amount](implicit divide: DivideByInt[T]): Divide[Pos[T], Int] =
    (a, b) => Pos(divide(a.x, b), divide(a.y, b))
}
