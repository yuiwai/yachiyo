package com.yuiwai.yachiyo.core

import utest._

object DrawingSpec extends TestSuite {
  val tests = Tests {
    "circle" - {
      Drawing.circle(10).size ==> 400
      Drawing.circle(2.5).size ==> 16
    }
    "line" - {
      Drawing.line(Pos(0, 0), Pos(2, 2), 1).size ==> 9
      Drawing.line(Pos(0, 0), Pos(0, 5), 1).values ==> Seq(1, 1, 1, 1, 1)
      Drawing.line(Pos(0, 0), Pos(5, 0), 1).values ==> Seq(1, 1, 1, 1, 1)
    }
  }
}
