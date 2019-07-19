package com.yuiwai.yachiyo.core

import utest._

object BlockSpec extends TestSuite {
  val tests = Tests {
    "creation" - {
      "empty" - {
        Block.empty.size ==> 0
      }
      "fill" - {
        val b = Block.fill(3, 3, false)
        b == Block.fillSquare(3, false)
        b.size ==> 9
        b.foreach(_ ==> false)
      }
      "fillWithIndex" - {
        Block.fillWithIndex(2, 2)(identity).values ==> Seq(0, 1, 2, 3)
        Block.fillWithIndex(2, 3)(identity).values ==> Seq(0, 1, 2, 3, 4, 5)
      }
      "fillWithPos" - {
        Block.fillWithPos(2, 2)(identity).values ==> Seq(Pos(0, 0), Pos(1, 0), Pos(0, 1), Pos(1, 1))
      }
      "fillZero" - {
        // TODO webglがマージされたら対応
        // Block.fillZero[Int](2, 2).values ==> Seq(0, 0, 0, 0)
      }
    }
    "values with pos" - {
      Block.fillWithIndex(2, 2)(identity).valuesWithPos ==>
        Seq(Pos(0, 0) -> 0, Pos(1, 0) -> 1, Pos(0, 1) -> 2, Pos(1, 1) -> 3)
    }
    "map" - {
      Block.fill(3, 3, 1).map(_ + 1) ==> BlockImpl(3, 3, Seq(2, 2, 2, 2, 2, 2, 2, 2, 2))
      Block.fill(2, 2, 'a').map(_ + "bcd").values ==> Seq("abcd", "abcd", "abcd", "abcd")
      Block.fillWithIndex(2, 2)(identity).map(_ * 2).values ==> Seq(0, 2, 4, 6)
    }
    "iterator" - {
      "empty" - {
        val itr = Block.empty.iterator
        itr.hasNext ==> false
      }
      "non empty" - {
        val itr = Block.fillSquare(5, 1).iterator
        itr.value ==> 1
        itr.hasNext ==> true
        itr.next.value ==> 1
      }
    }
    "row" - {
      val b = Block.fillWithIndex(2, 3)(identity)
      b.row(-1) ==> None
      b.row(0) ==> Some(Row(0, 1))
      b.row(1) ==> Some(Row(2, 3))
      b.row(2) ==> Some(Row(4, 5))
      b.row(3) ==> None
    }
    "region" - {
      val b = Block.fillWithIndex(3, 4)(identity)

      {
        val r = b.region(Pos(1, 1), 2, 2)
        r.size ==> 4
      }

      {
        val r = b.region(Pos(0, 1), 2, 3)
        r.size ==> 6
      }

      {
        val r = b.region(Pos(3, 3), 2, 2)
        r.size ==> 2
      }
    }
    "updated" - {
      Block.fillSquare(2, 0).updated(Pos(1, 1), 1).values ==> Seq(0, 0, 0, 1)
      Block.fillSquare(2, 0).updated(Pos(3, 1), 1).values ==> Seq(0, 0, 0, 0)
    }
    "patch" - {
      Block.fillSquare(3, 0).patch(Pos(0, 0), Block.fill(2, 2, 1)).values ==>
        Seq(1, 1, 0, 1, 1, 0, 0, 0, 0)
    }
    "concat" - {
      Block.fillSquare(2, 2).concatX(Block.fillSquare(3, 3)) ==> None
      Block.fillSquare(2, 0).concatX(Block.fill(1, 2, 1)).get.values ==>
        Seq(0, 0, 1, 0, 0, 1)

      Block.fillSquare(2, 2).concatY(Block.fillSquare(3, 3)) ==> None
      Block.fillSquare(2, 0).concatY(Block.fill(2, 1, 1)).get.values ==>
        Seq(0, 0, 0, 0, 1, 1)
    }
    "flip" - {
      Block.fillWithIndex(2, 2)(identity).flipX.values ==> Seq(1, 0, 3, 2)
      Block.fillWithIndex(2, 2)(identity).flipY.values ==> Seq(2, 3, 0, 1)
    }
    "find" - {
      Block.fillWithIndex(2, 2)(identity).find(_ % 2 == 1) ==> Some(1)
      Block.fillWithIndex(2, 2)(identity).find(_ == 10) ==> None
    }
    "expansion" - {
    }
    "shrink" - {
    }
  }
}
