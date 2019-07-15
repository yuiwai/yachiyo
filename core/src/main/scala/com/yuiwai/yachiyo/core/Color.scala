package com.yuiwai.yachiyo.core

trait Color

final case class RGB(r: Int, g: Int, b: Int) extends Color {
  def toHexString: String = (r :: g :: b :: Nil)
    .map(_.toHexString match {
      case s if s.length == 1 => s"0$s"
      case s => s
    })
    .mkString
  def inverted: RGB = RGB(255 - r, 255 - g, 255 - b)
  def mix(that: RGB, rate: Double): RGB =
    RGB(((r + that.r) / rate).toInt, ((g + that.g) / rate).toInt, ((b + that.b) / rate).toInt)
}

final case class HLS(h: Int, l: Int, s: Int) extends Color
