package com.yuiwai.yachiyo.core

object Drawing {
  def circle[T](radius: T)(implicit toDouble: ToDouble[T]): Block[Double] = {
    val br = Block.fillWithDistance(toDouble(radius).toInt, toDouble(radius).toInt) {
      d =>
        if (toDouble(radius) > d) 1.0
        else (1.0 + toDouble(radius) - d).max(0)
    }
    val b = br.flipX.concatX(br).get
    b.flipY.concatY(b).get
  }
  def line[T](from: Pos[T], to: Pos[T], weight: T)
    (implicit toDouble: ToDouble[T]): Block[Double] = {
    (from.map(toDouble(_)), to.map(toDouble(_))) match {
      case (fp, tp) =>
        val e = Linear(fp.x, fp.y, tp.x, tp.y)
        Block.fillWithPos(
          (fp.x.max(tp.x) - fp.x.min(tp.x)).toInt,
          (fp.y.max(tp.y) - fp.y.min(tp.y)).toInt
        )(p => (toDouble(weight) - Math.abs(e(p.x + .5) - p.y)).max(0) / toDouble(weight))
    }
  }
}
