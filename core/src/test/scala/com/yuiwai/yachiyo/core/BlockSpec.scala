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
      "fillTile" - {
        val b = Block.fillTile(4, 4, Block.fillWithIndex(2, 2)(identity))
        b.values ==> Seq(0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3)
      }
      "fillWithIndex" - {
        Block.fillWithIndex(2, 2)(identity).values ==> Seq(0, 1, 2, 3)
        Block.fillWithIndex(2, 3)(identity).values ==> Seq(0, 1, 2, 3, 4, 5)
      }
      "fillWithPos" - {
        Block.fillWithPos(2, 2)(identity).values ==> Seq(Pos(0, 0), Pos(1, 0), Pos(0, 1), Pos(1, 1))
      }
      "fillWithDistance" - {
        Block.fillWithDistance(2, 2)(d => (d * 10).toInt).values ==> Seq(14, 22, 22, 28)
      }
      "fillZero" - {
        Block.fillZero[Int](2, 2).values ==> Seq(0, 0, 0, 0)
      }
    }
    "values with pos" - {
      Block.fillWithIndex(2, 2)(identity).valuesWithPos ==>
        Seq(Pos(0, 0) -> 0, Pos(1, 0) -> 1, Pos(0, 1) -> 2, Pos(1, 1) -> 3)
    }
    "map" - {
      Block.fill(3, 3, 1).map(_ + 1) ==> BlockImpl(3, 3, Vector(2, 2, 2, 2, 2, 2, 2, 2, 2))
      Block.fill(2, 2, 'a').map(_ + "bcd").values ==> Seq("abcd", "abcd", "abcd", "abcd")
      Block.fillWithIndex(2, 2)(identity).map(_ * 2).values ==> Seq(0, 2, 4, 6)
    }
    "mapRow" - {
      val b = Block.fill(3, 3, 1).mapRow(2 +: _).get
      b.size ==> 12
      b.rows.forall(_ == Seq(2, 1, 1, 1)) ==> true
    }
    "mapCol" - {
      val b = Block.fill(3, 3, 1).mapCol(2 +: _).get
      b.size ==> 12
      b.values ==> Seq(2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
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
      b.row(0) ==> Some(Seq(0, 1))
      b.row(1) ==> Some(Seq(2, 3))
      b.row(2) ==> Some(Seq(4, 5))
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
      val b1 = Block.fillSquare(2, 0).concatX(Block.fill(1, 2, 1)).get
      b1.width ==> 3
      b1.values ==> Seq(0, 0, 1, 0, 0, 1)

      Block.fillWithIndex(2, 2)(identity)
        .concatX(Block.fillWithIndex(2, 2)(identity))
        .get.values ==> Seq(0, 1, 0, 1, 2, 3, 2, 3)

      Block.fillSquare(2, 2).concatY(Block.fillSquare(3, 3)) ==> None
      val b2 = Block.fillSquare(2, 0).concatY(Block.fill(2, 1, 1)).get
      b2.height ==> 3
      b2.values ==> Seq(0, 0, 0, 0, 1, 1)
    }
    "flip" - {
      Block.fillWithIndex(2, 2)(identity).flipX.values ==> Seq(1, 0, 3, 2)
      Block.fillWithIndex(2, 2)(identity).flipY.values ==> Seq(2, 3, 0, 1)
    }
    "find" - {
      Block.fillWithIndex(2, 2)(identity).find(_ % 2 == 1) ==> Some(1)
      Block.fillWithIndex(2, 2)(identity).find(_ == 10) ==> None
    }
    "rotate" - {
      Block.fillWithIndex(2, 2)(identity).rotateR.values ==> Seq(2, 0, 3, 1)
      Block.fillWithIndex(2, 2)(identity).rotateL.values ==> Seq(1, 3, 0, 2)
    }
    "add" - {
      Block.fill(2, 2, 1) + Block.fill(2, 2, 2) ==>
        Some(Block.fill(2, 2, 3))
      Block.fill(2, 2, 1.0) + Block.fill(2, 2, 2.0) ==>
        Some(Block.fill(2, 2, 3.0))
    }
    "bitBlock" - {
      val b = Block.fillWithIndex(3, 3)(identity)
      val m1 = Block.fill(3, 3, true)
      val m0 = Block.fill(3, 3, false)
      m1.mask(b) ==> b
      m0.mask(b) ==> Block.fillZero[Int](3, 3)
    }
    "resizeX" - {
      Block.resizeX(0, 0, Seq.empty[Int]) ==> Seq.empty
      Block.resizeX(0, 0, Seq(1)) ==> Seq.empty
      Block.resizeX(1, 0, Seq.empty[Int]) ==> Seq.empty
      Block.resizeX(1, 0, Seq(1)) ==> Seq(1)
      Block.resizeX(2, 0, Seq(1)) ==> Seq(1, 1)
      Block.resizeX(2, 0, Seq(1, 2)) ==> Seq(1, 2)
      Block.resizeX(2, 0, Seq(1, 2, 3)) ==> Seq(1, 3)
      Block.resizeX(3, 0, Seq(1.0, 2.0)) ==> Seq(1.0, 1.5, 2.0)
      Block.resizeX(3, 0, Seq(1.0, 2.0, 3.0, 4.0)) ==> Seq(1.0, 2.5, 4.0)
    }
    "resizeTo" - {
      val b1 = Block.withValues(2, Seq(1.0, 3.0, 5.0, 7.0))
      val b2 = Block.withValues(3, Seq(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0))
      b1.resizeTo(2, 2).get ==> b1
      b1.resizeTo(3, 2).get.values ==> Seq(1.0, 2.0, 3.0, 5.0, 6.0, 7.0)
      b1.resizeTo(2, 3).get.values ==> Seq(1.0, 3.0, 3.0, 5.0, 5.0, 7.0)

      b2.resizeTo(3, 3).get ==> b2
      b2.resizeTo(2, 3).get.values ==> Seq(1.0, 3.0, 4.0, 6.0, 7.0, 9.0)
      b2.resizeTo(3, 2).get.values ==> Seq(1.0, 2.0, 3.0, 7.0, 8.0, 9.0)
    }
    "clipping" - {
    }
  }
}
