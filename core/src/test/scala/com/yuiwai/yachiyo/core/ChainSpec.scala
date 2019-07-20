package com.yuiwai.yachiyo.core

import utest._

object ChainSpec extends TestSuite {
  val tests = Tests {
    "chain" - {
      val c1 = Anchor[Pos[Float], Pos[Float]](Pos(2f, 2f)).chain(Pos.zero)
      c1.size ==> 1
      c1.head ==> Pos(2, 2)

      val c2 = c1.stop(Anchor(Pos(4, 6)))
      c2.size ==> 1
      c2.head ==> Pos(3, 4)

      val c3 = c2.chain(Pos.zero)
      c3.size ==> 2
      c3.head.map(_.toInt) ==> Pos(2, 3)
      c3.tail.head.map(_.toInt) ==> Pos(3, 4)
    }
  }
}
