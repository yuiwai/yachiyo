package com.yuiwai.yachiyo.core

trait Equation[T] {
  def apply(x: T): T
}

final case class Linear[T](a: T, b: T)(implicit multiply: Multiply[T, T], plus: Plus[T])
  extends Equation[T] {
  override def apply(x: T): T = plus(multiply(x, a), b)
}
object Linear {
  def apply[T: Plus](x1: T, y1: T, x2: T, y2: T)
    (implicit minus: Minus[T], divide: Divide[T, T], multiply: Multiply[T, T]): Linear[T] = {
    val a = divide(minus(y2, y1), minus(x2, x1))
    apply(a, minus(y1, multiply(x1, a)))
  }
}

final case class Fraction(numerator: Int, denominator: Int)
object Fraction {
  def normal(numerator: Int, denominator: Int): Fraction =
    normalImpl(numerator, denominator, 2)
  private def normalImpl(numerator: Int, denominator: Int, i: Int): Fraction =
    if (i > numerator || i > denominator) Fraction(numerator, denominator)
    else (numerator % i, denominator % i, numerator / i, denominator / i) match {
      case (0, 0, n, d) => normalImpl(n, d, i)
      case _ => normalImpl(numerator, denominator, i + 1)
    }
}

