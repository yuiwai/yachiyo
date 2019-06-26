package com.yuiwai.yachiyo.core

import scala.language.higherKinds

trait Sensor[F[_], P, In, Out] {
  def mix(sight: Sight[F, P], subject: F[In]): F[Out]
}

trait Lens[F[_], S, P] {
  def focus(scale: Scale[S], center: P): Sight[F, P]
}

final case class Scale[S](value: S)

trait Sight[F[_], P] {
  def collect(position: P): Option[P]
}

object Shutter {
  final case class Ready[F[_], S, P, In, Out](scale: Scale[S], center: P) {
    def release(subject: F[In])
      (implicit lens: Lens[F, S, P], sensor: Sensor[F, P, In, Out]): F[Out] = Shutter.release(scale)(center)(subject)
  }
  def ready[F[_], S, P, In, Out](scale: Scale[S], center: P): Ready[F, S, P, In, Out] = Ready(scale, center)
  def release[F[_], S, P, In, Out](scale: Scale[S])(center: P)(subject: F[In])
    (implicit lens: Lens[F, S, P], sensor: Sensor[F, P, In, Out]): F[Out] = sensor.mix(lens.focus(scale, center), subject)
}

trait Bounded[V] {
  def contain(value: V): Boolean
}
abstract class ScalarBound[V](min: V, max: V)(implicit ord: Ordering[V]) extends Bounded[V] {
  override def contain(value: V): Boolean = ord.lt(value, max) && ord.gt(value, min)
}
abstract class RectangleBound[P, V](x: P => V, y: P => V, start: P, end: P)
  (implicit ord: Ordering[V]) extends Bounded[P] {
  override def contain(value: P): Boolean = ord.lt(x(value), x(end)) &&
    ord.lt(y(value), y(end)) &&
    ord.gt(x(value), x(start)) &&
    ord.gt(y(value), y(start))
}

object Aggregator {
  def sum[V: Add](values: Seq[V]): V = values.reduce(implicitly[Add[V]].apply)
  def avg[L: Add, R](values: Seq[L])(implicit divide: Divide[L, Int]): L = divide(sum(values), values.size)
}

trait Add[V] {
  def apply(l: V, r: V): V
}
object Add {
  implicit val intAdd: Add[Int] = _ + _
}
trait Divide[L, R] {
  def apply(l: L, r: R): L
}
object Divide {
  implicit val intDivide: Divide[Int, Int] = _ / _
}
