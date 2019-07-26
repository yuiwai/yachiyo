package com.yuiwai.yachiyo.core

import utest._

object EquationSpec extends TestSuite {
  val tests = Tests {
    "linear" - {
      val l1 = Linear(1.0, 0.0)
      l1(1) ==> 1.0
      l1(2) ==> 2.0

      val l2 = Linear(2.0, 0.0)
      l2(1) ==> 2.0
      l2(2) ==> 4.0

      val l3 = Linear(-2, 10)
      l3(1) ==> 8
      l3(2) ==> 6

      val l4 = Linear(0, 10)
      l4(1) ==> 10
      l4(2) ==> 10

      val l5 = Linear(10.0, 10.0, 10.0, 20.0)

      Linear(1.0, 1.0, 2.0, 2.0) ==> l1
      Linear(1.0, 2.0, 2.0, 4.0) ==> l2
      Linear(1, 8, 2, 6) ==> l3
      Linear(10, 10, 20, 10) ==> l4
    }
  }
}

object FractionSpec extends TestSuite {
  val tests = Tests {
    "normal" - {
      Fraction.normal(2, 4) ==> Fraction(1, 2)
      Fraction.normal(9, 3) ==> Fraction(3, 1)
      Fraction.normal(19 * 17, 13 * 17) ==> Fraction(19, 13)
    }
  }
}
