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
        b == Block.fill(3, false)
        b.size ==> 9
        b.foreach(_ ==> false)
      }
    }
    "iterator" - {
      "empty" - {
        val itr = Block.empty.iterator
        itr.hasNext ==> false
      }
      "non empty" - {
      val itr = Block.fill(5, 1).iterator
      itr.value ==> 1
      itr.hasNext ==> true
      itr.next.value ==> 1
      }
    }
    "row" - {

    }
    "region" - {
    }
    "update" - {
    }
    "join" - {
    }
  }
}
