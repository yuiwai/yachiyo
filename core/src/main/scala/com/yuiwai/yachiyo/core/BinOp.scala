package com.yuiwai.yachiyo.core

trait BinOp[L, R, A] {
  def apply(l: L, r: R): A
}

trait Plus[T] extends BinOp[T, T, T] {
  def apply(l: T, r: T): T
}
object Plus {
  implicit val intPlus: Plus[Int] = _ + _
  implicit val floatPlus: Plus[Float] = _ + _
  implicit val doublePlus: Plus[Double] = _ + _
  implicit val boolPlus: Plus[Boolean] = {
    case (false, false) => false
    case _ => true
  }
  implicit def posPlus[T: Amount](implicit plus: Plus[T]): Plus[Pos[T]] = (a, b) => Pos(plus(a.x, b.x), plus(a.y, b.y))
  implicit def listPlus[T]: Plus[List[T]] = _ ++ _
}

trait Multiply[L, R] extends BinOp[L, R, L] {
  def apply(l: L, r: R): L
}
object Multiply {
  implicit val intDouble: Multiply[Int, Double] = { (i, d) => (i * d).toInt }
  implicit val intInt: Multiply[Int, Int] = _ * _
  implicit val doubleDouble: Multiply[Double, Double] = _ * _
  implicit val boolBool: Multiply[Boolean, Boolean] = {
    case (true, true) => true
    case _ => false
  }
}
trait Minus[T] extends BinOp[T, T, T] {
  def apply(l: T, r: T): T
}

object Minus {
  implicit val intMinus: Minus[Int] = _ - _
  implicit val floatMinus: Minus[Float] = _ - _
  implicit val doubleMinus: Minus[Double] = _ - _
  implicit val boolMinus: Minus[Boolean] = {
    case (true, false) => true
    case _ => false
  }
  implicit def posMinus[T: Amount](implicit minus: Minus[T]): Minus[Pos[T]] =
    (a, b) => Pos(minus(a.x, b.x), minus(a.y, b.y))
}

trait Divide[L, R] extends BinOp[L, R, L] {
  def apply(l: L, r: R): L
}
object Divide {
  type DivideByInt[T] = Divide[T, Int]
  implicit val intDivide: Divide[Int, Int] = _ / _
  implicit val doubleDivide: Divide[Double, Double] = _ / _
  implicit val floatIntDivide: Divide[Float, Int] = _ / _
  implicit val doubleIntDivide: Divide[Double, Int] = _ / _
  implicit val boolDivide: Divide[Boolean, Boolean] = {
    case (true, true) => true
    case _ => false
  }
  implicit def posIntDivide[T: Amount](implicit divide: DivideByInt[T]): Divide[Pos[T], Int] =
    (a, b) => Pos(divide(a.x, b), divide(a.y, b))
}
