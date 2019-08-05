package com.yuiwai.yachiyo.core

import utest._

object MosaicSpec extends TestSuite {
  val tests = Tests {
    "seq" - {
      implicit val intAvg: Aggregation[Seq, Int] = (data: Seq[Int]) => data.sum / data.size
      implicit val doubleAvg: Aggregation[Seq, Double] = (data: Seq[Double]) => data.sum / data.size
      Mosaic[Seq, Int](1 to 8, 2) ==> Seq(1, 1, 3, 3, 5, 5, 7, 7)
      Mosaic[Seq, Int](1 to 5, 2) ==> Seq(1, 1, 3, 3, 5, 5)
      Mosaic[Seq, Double]((1 to 5).map(_.toDouble), 3).toList ==> List(2.0, 2.0, 2.0, 4.5, 4.5, 4.5)
    }
  }
}
