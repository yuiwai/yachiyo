package com.yuiwai.yachiyo.core

import scala.language.higherKinds

trait Aggregation[F[_], T] {
  def aggregate(data: F[T]): T
}
object Aggregation {
  def sum[V: Plus](values: Seq[V]): V = values.reduce(implicitly[Plus[V]].apply)
  def avg[L: Plus, R](values: Seq[L])(implicit divide: Divide[L, Int]): L = divide(sum(values), values.size)
  def mid[V](values: Seq[V]): V = values(values.size / 2)
  def max[V: Ordering](values: Seq[V]): V = values.max
  def min[V: Ordering](values: Seq[V]): V = values.min
}
