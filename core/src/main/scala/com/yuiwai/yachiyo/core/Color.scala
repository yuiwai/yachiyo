package com.yuiwai.yachiyo.core

trait Color extends Any

trait RGBOps extends Any with Color {
  def value: Int
  def r: Int = value & 0xff
  def g: Int = value >> 8 & 0xff
  def b: Int = value >> 16 & 0xff
  def map(f: Int => Int): RGBOps
}

final case class RGB(value: Int) extends AnyVal with RGBOps {
  def map(f: Int => Int): RGB = RGB(f(r), f(g), f(b))
  def map2(rgb: RGB)(f: (Int, Int) => Int): RGB = RGB(f(r, rgb.r), f(g, rgb.g), f(b, rgb.b))
  def toHexString: String = (r :: g :: b :: Nil)
    .map(_.toHexString match {
      case s if s.length == 1 => s"0$s"
      case s => s
    })
    .mkString
  def inverted: RGB = RGB(value ^ 0xffffff)
  def mix(that: RGB, rate: Double = 2.0): RGB = map2(that)((a, b) => ((a + b) / rate).toInt)
  def +(that: RGB): RGB = map2(that)((a, b) => (a + b).min(255))
  def -(that: RGB): RGB = map2(that)((a, b) => (a - b).max(0))
  def *(that: RGB): RGB = map2(that)(_ * _ / 255)
  def max(that: RGB): RGB = map2(that)(_.max(_))
  def min(that: RGB): RGB = map2(that)(_.min(_))
  override def toString: String = s"RGB($r, $g, $b)"
}
object RGB {
  def apply(r: Int, g: Int, b: Int): RGB = apply(r | g << 8 | b << 16)
  val White = apply(255, 255, 255)
  val Black = apply(0, 0, 0)
  val Red = apply(255, 0, 0)
  val Green = apply(0, 255, 0)
  val Blue = apply(0, 0, 255)
}

final case class RGBA(value: Int) extends AnyVal with RGBOps {
  def a: Int = value >> 24 & 0xff
  override def map(f: Int => Int): RGBA = RGBA(f(r), f(g), f(b), f(a))
  def alphaBlend(that: RGBA): RGBA = RGBA(
    if (that.a == 0) r else if (that.a == 255) that.r else (that.r * that.a + r * a) / 510,
    if (that.a == 0) g else if (that.a == 255) that.g else (that.g * that.a + g * a) / 510,
    if (that.a == 0) b else if (that.a == 255) that.r else (that.b * that.a + b * a) / 510,
    255
  )
}
object RGBA {
  def apply(r: Int, g: Int, b: Int, a: Int): RGBA = apply((r & 0xff) | (g & 0xff) << 8 | (b & 0xff) << 16 | (a & 0xff) << 24)
}

// final case class HLS(h: Int, l: Int, s: Int) extends Color
