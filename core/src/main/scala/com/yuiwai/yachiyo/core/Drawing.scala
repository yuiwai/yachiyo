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
        if (fp.x == tp.x) {
          Block.fillWithPos(toDouble(weight).toInt, Math.abs(tp.y - fp.y).toInt)(_ => 1)
        } else if (fp.y == tp.y) {
          Block.fillWithPos(Math.abs(tp.x - fp.x).toInt, toDouble(weight).toInt)(_ => 1)
        } else {
          val e = Linear(fp.x, fp.y, tp.x, tp.y)
          Block.fillWithPos(
            (Math.abs(fp.x - tp.x) + toDouble(weight)).toInt,
            (Math.abs(fp.y - tp.y) + toDouble(weight)).toInt
          )(p => (toDouble(weight) - Math.abs(e(p.x) - p.y)).max(0) * toDouble(weight))
        }
    }
  }
}

final case class Path(from: Pos[Int], to: Pos[Int]) {
  def toList: List[Pos[Int]] = {
    if (from == to) from :: Nil
    else {
      val o = Pos((from.x + to.x) / 2, (from.y + to.y) / 2)
      if (from == o || to == o) from :: to :: Nil
      else Path(from, o).toList ++ Path(o, to).toList.tail
    }
  }
}
