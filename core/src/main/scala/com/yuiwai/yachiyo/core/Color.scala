package com.yuiwai.yachiyo.core

trait Color

final case class RGB(r: Int, g: Int, b: Int) extends Color {
  def toHexString: String = (r :: g :: b :: Nil)
    .map(_.toHexString match {
      case s if s.length == 1 => s"0$s"
      case s => s
    })
    .mkString
}

final case class HLS(h: Int, l: Int, s: Int) extends Color
