package com.yuiwai.yachiyo.core

object Drawing {
  def circle(radius: Int): Block[Boolean] = {
    val br = Block.fillWithDistance(radius, radius)(_ <= radius)
    val b = br.flipX.concatX(br).get
    b.flipY.concatY(b).get
  }
}
