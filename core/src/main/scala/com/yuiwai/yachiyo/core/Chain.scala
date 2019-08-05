package com.yuiwai.yachiyo.core

import com.yuiwai.yachiyo.core.Divide.DivideByInt

trait Chain

final class Chaining[P: Plus : Minus : DivideByInt, V](from: Anchor[P, V], to: Option[Anchor[P, V]], values: Seq[V])
  (implicit p: Positioning[P, V]) {
  def size: Int = values.size
  def head: V = values.head
  def tail: Seq[V] = values.tail
  def stop(anchor: Anchor[P, V]): Chaining[P, V] = Chaining(from, anchor, values)
  def chain(value: V): Chaining[P, V] = to match {
    case Some(anchor) => Chaining(from, anchor, values :+ value)
    case None => Chaining(from, values :+ value)
  }
}
object Chaining {
  def apply[P, V](from: Anchor[P, V], to: Anchor[P, V], values: Seq[V])
    (implicit p: Positioning[P, V], plus: Plus[P], minus: Minus[P], divide: Divide[P, Int]): Chaining[P, V] = {
    val d = divide(minus(p.get(to.value), p.get(from.value)), values.size + 1)
    new Chaining(from, Some(to), values.foldLeft(Seq.empty[V] -> d) {
      case ((seq, dist), v) => (seq :+ p.set(v, plus(p.get(from.value), dist))) -> plus(dist, dist)
    }._1)
  }
  def apply[P: Plus : Minus : DivideByInt, V](from: Anchor[P, V], values: Seq[V])
    (implicit p: Positioning[P, V]): Chaining[P, V] =
    new Chaining(from, None, values.map(v => p.set(v, p.get(from.value))))
}

trait Positioning[P, V] {
  def get(v: V): P
  def set(v: V, p: P): V
}
object Positioning {
  implicit def pos2pos[T: Amount]: Positioning[Pos[T], Pos[T]] = new Positioning[Pos[T], Pos[T]] {
    override def get(v: Pos[T]): Pos[T] = v
    override def set(v: Pos[T], p: Pos[T]): Pos[T] = p
  }
}

trait ChainOps[P, V] extends Any {
  def chain(target: V)(implicit p: Positioning[P, V]): Chaining[P, V]
  // def chain(anchor: Anchor[P, V]): Chaining[P, V]
}

final case class Anchor[P: Plus : Minus : DivideByInt, V](value: V) extends ChainOps[P, V] {
  override def chain(target: V)(implicit p: Positioning[P, V]): Chaining[P, V] = Chaining(this, Seq(target))
}
