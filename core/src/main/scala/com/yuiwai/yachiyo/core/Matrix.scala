package com.yuiwai.yachiyo.core

trait Matrix

final case class Mat3[T](
  x11: T, x12: T, x13: T,
  x21: T, x22: T, x23: T,
  x31: T, x32: T, x33: T)
  (implicit add: Add[T], multiply: Multiply[T, T]) extends Matrix {
  def row1: Vec3[T] = Vec3(x11, x12, x13)
  def row2: Vec3[T] = Vec3(x21, x22, x23)
  def row3: Vec3[T] = Vec3(x31, x32, x33)
  def col1: Vec3[T] = Vec3(x11, x21, x31)
  def col2: Vec3[T] = Vec3(x12, x22, x32)
  def col3: Vec3[T] = Vec3(x13, x23, x33)
  def +(that: Mat3[T]): Mat3[T] =
    Mat3(
      add(x11, that.x11), add(x12, that.x12), add(x13, that.x13),
      add(x21, that.x21), add(x22, that.x22), add(x23, that.x23),
      add(x31, that.x31), add(x32, that.x32), add(x33, that.x33)
    )
  def *(v: T): Mat3[T] =
    Mat3(
      multiply(x11, v), multiply(x12, v), multiply(x13, v),
      multiply(x21, v), multiply(x22, v), multiply(x23, v),
      multiply(x31, v), multiply(x32, v), multiply(x33, v)
    )
  def *(that: Mat3[T]): Mat3[T] = Mat3[T](
    add(add(multiply(x11, that.x11), multiply(x12, that.x21)), multiply(x13, that.x31)),
    add(add(multiply(x11, that.x12), multiply(x12, that.x22)), multiply(x13, that.x32)),
    add(add(multiply(x11, that.x13), multiply(x12, that.x23)), multiply(x13, that.x33)),

    add(add(multiply(x21, that.x11), multiply(x22, that.x21)), multiply(x23, that.x31)),
    add(add(multiply(x21, that.x12), multiply(x22, that.x22)), multiply(x23, that.x32)),
    add(add(multiply(x21, that.x13), multiply(x22, that.x23)), multiply(x23, that.x33)),

    add(add(multiply(x31, that.x11), multiply(x32, that.x21)), multiply(x33, that.x31)),
    add(add(multiply(x31, that.x12), multiply(x32, that.x22)), multiply(x33, that.x32)),
    add(add(multiply(x31, that.x13), multiply(x32, that.x23)), multiply(x33, that.x33))
  )
  def *(that: Vec3[T]): Vec3[T] = Vec3[T](
    add(add(multiply(x11, that.x1), multiply(x12, that.x2)), multiply(x13, that.x3)),
    add(add(multiply(x21, that.x1), multiply(x22, that.x2)), multiply(x23, that.x3)),
    add(add(multiply(x31, that.x1), multiply(x32, that.x2)), multiply(x33, that.x3))
  )
}
object Mat3 {
  type Multi[T] = Multiply[T, T]
  def apply[T: Add : Multi](v1: T, v2: T, v3: T): Mat3[T] = apply(v1, v1, v1, v2, v2, v2, v3, v3, v3)
  def zero[T: Add : Multi](implicit z: Zero[T]): Mat3[T] = fill(z())
  def unit[T: Add : Multi : Zero](implicit u: UNIT[T]): Mat3[T] = zero[T].copy(x11 = u(), x22 = u(), x33 = u())
  def fill[T: Add : Multi](v: T): Mat3[T] = apply(v, v, v, v, v, v, v, v, v)
}

final case class Vec3[T](x1: T, x2: T, x3: T)(implicit add: Add[T], multiply: Multiply[T, T]) {
  def +(that: Vec3[T]): Vec3[T] = Vec3(add(x1, that.x1), add(x2, that.x2), add(x3, that.x3))
  def *(v: T): Vec3[T] = Vec3(multiply(x1, v), multiply(x2, v), multiply(x3, v))
  def *(that: Vec3[T]): T = add(add(multiply(x1, that.x1), multiply(x2, that.x2)), multiply(x3, that.x3))
}
object Vec3 {
  type Multi[T] = Multiply[T, T]
  def apply[T: Add : Multi](v: T): Vec3[T] = apply(v, v, v)
  def zero[T: Add : Multi](implicit z: Zero[T]): Vec3[T] = apply(z())
  def unit[T: Add : Multi](implicit u: UNIT[T]): Vec3[T] = apply(u())
}

trait Zero[T] {
  def apply(): T
}
object Zero {
  implicit val intZero: Zero[Int] = () => 0
  implicit val doubleZero: Zero[Double] = () => 0.0
}

trait UNIT[T] {
  def apply(): T
}
object UNIT {
  implicit val intUnit: UNIT[Int] = () => 1
  implicit val doubleUnit: UNIT[Double] = () => 1.0
}

