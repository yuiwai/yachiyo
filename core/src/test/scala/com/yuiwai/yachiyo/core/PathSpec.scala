package com.yuiwai.yachiyo.core

import utest._

object PathSpec extends TestSuite {
  val tests = Tests {
    "horizon line" - {
      Path(Pos(1, 1), Pos(5, 1)).toList ==> List(Pos(1, 1), Pos(2, 1), Pos(3, 1), Pos(4, 1), Pos(5, 1))
      Path(Pos(3, 1), Pos(1, 1)).toList ==> List(Pos(3, 1), Pos(2, 1), Pos(1, 1))
      Path(Pos(-1, 2), Pos(1, 2)).toList ==> List(Pos(-1, 2), Pos(0, 2), Pos(1, 2))
    }
    "vertical line" - {
      Path(Pos(2, 1), Pos(2, 3)).toList ==> List(Pos(2, 1), Pos(2, 2), Pos(2, 3))
      Path(Pos(3, 1), Pos(3, -1)).toList ==> List(Pos(3, 1), Pos(3, 0), Pos(3, -1))
    }
    "oblique line" - {
      Path(Pos(1, 1), Pos(4, 3)).toList ==> List(Pos(1, 1), Pos(2, 2), Pos(3, 2), Pos(4, 3))
    }
    "poly line" - {

    }
  }
}
