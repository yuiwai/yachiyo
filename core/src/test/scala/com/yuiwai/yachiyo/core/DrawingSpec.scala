package com.yuiwai.yachiyo.core

import utest._

object DrawingSpec extends TestSuite {
  val tests = Tests {
    "circle" - {
      Drawing.circle(10).size ==> 400
      Drawing.circle(2.5).size ==> 16
    }
    "line" - {
      val l = Drawing.line(Pos(0, 0), Pos(2, 2), 1)
      l.size ==> 4
    }
  }
}
