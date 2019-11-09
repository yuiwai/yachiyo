package com.yuiwai.yachiyo.core

trait BinOp[L, R, A] {
  def apply(l: L, r: R): A
}

trait Plus[T] extends BinOp[T, T, T] {
  def apply(l: T, r: T): T
}
object Plus {
  implicit val intPlus: Plus[Int] = new Plus[Int] {
    override def apply(l: Int, r: Int): Int = l + r
  }
  implicit val floatPlus: Plus[Float] = new Plus[Float] {
    override def apply(l: Float, r: Float): Float = l + r
  }
  implicit val doublePlus: Plus[Double] = new Plus[Double] {
    override def apply(l: Double, r: Double): Double = l + r
  }
  implicit val boolPlus: Plus[Boolean] = new Plus[Boolean] {
    override def apply(l: Boolean, r: Boolean): Boolean = (l, r) match {
      case (false, false) => false
      case _ => true
    }
  }
  implicit def posPlus[T: Amount](implicit plus: Plus[T]): Plus[Pos[T]] = new Plus[Pos[T]] {
    override def apply(l: Pos[T], r: Pos[T]): Pos[T] = Pos(plus(l.x, r.x), plus(l.y, r.y))
  }
  implicit def listPlus[T]: Plus[List[T]] = new Plus[List[T]] {
    override def apply(l: List[T], r: List[T]): List[T] = l ++ r
  }
}

trait Multiply[L, R] extends BinOp[L, R, L] {
  def apply(l: L, r: R): L
}
object Multiply {
  implicit val intDouble: Multiply[Int, Double] = new Multiply[Int, Double] {
    override def apply(l: Int, r: Double): Int = l * r.toInt
  }
  implicit val intInt: Multiply[Int, Int] = new Multiply[Int, Int] {
    override def apply(l: Int, r: Int): Int = l * r
  }
  implicit val doubleDouble: Multiply[Double, Double] = new Multiply[Double, Double] {
    override def apply(l: Double, r: Double): Double = l * r
  }
  implicit val boolBool: Multiply[Boolean, Boolean] = new Multiply[Boolean, Boolean] {
    override def apply(l: Boolean, r: Boolean): Boolean = (l, r) match {
      case (true, true) => true
      case _ => false
    }
  }
}
trait Minus[T] extends BinOp[T, T, T] {
  def apply(l: T, r: T): T
}

object Minus {
  implicit val intMinus: Minus[Int] = new Minus[Int] {
    override def apply(l: Int, r: Int): Int = l - r
  }
  implicit val floatMinus: Minus[Float] = new Minus[Float] {
    override def apply(l: Float, r: Float): Float = l - r
  }
  implicit val doubleMinus: Minus[Double] = new Minus[Double] {
    override def apply(l: Double, r: Double): Double = l - r
  }
  implicit val boolMinus: Minus[Boolean] = new Minus[Boolean] {
    override def apply(l: Boolean, r: Boolean): Boolean = (l, r) match {
      case (true, false) => true
      case _ => false
    }
  }
  implicit def posMinus[T: Amount](implicit minus: Minus[T]): Minus[Pos[T]] = new Minus[Pos[T]] {
    override def apply(l: Pos[T], r: Pos[T]): Pos[T] = Pos(minus(l.x, r.x), minus(l.y, r.y))
  }
}

trait Divide[L, R] extends BinOp[L, R, L] {
  def apply(l: L, r: R): L
}
object Divide {
  type DivideByInt[T] = Divide[T, Int]
  implicit val intDivide: Divide[Int, Int] = new Divide[Int, Int] {
    override def apply(l: Int, r: Int): Int = l / r
  }
  implicit val doubleDivide: Divide[Double, Double] = new Divide[Double, Double] {
    override def apply(l: Double, r: Double): Double = l / r
  }
  implicit val floatIntDivide: Divide[Float, Int] = new Divide[Float, Int] {
    override def apply(l: Float, r: Int): Float = l / r
  }
  implicit val doubleIntDivide: Divide[Double, Int] = new Divide[Double, Int] {
    override def apply(l: Double, r: Int): Double = l / r
  }
  implicit val boolDivide: Divide[Boolean, Boolean] = new Divide[Boolean, Boolean] {
    override def apply(l: Boolean, r: Boolean): Boolean = (l, r) match {
      case (true, true) => true
      case _ => false
    }
  }
  implicit def posIntDivide[T: Amount](implicit divide: DivideByInt[T]): Divide[Pos[T], Int] =
    new Divide[Pos[T], Int] {
      override def apply(l: Pos[T], r: Int): Pos[T] = (l, r) match {
        case (a, b) => Pos(divide(a.x, b), divide(a.y, b))
      }
    }
}
