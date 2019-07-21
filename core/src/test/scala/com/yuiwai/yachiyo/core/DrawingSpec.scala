package com.yuiwai.yachiyo.core

import utest._

object DrawingSpec extends TestSuite {
  val tests = Tests {
    "circle" - {
      val c = Drawing.circle(10)
      c.size ==> 400
    }
  }
}
