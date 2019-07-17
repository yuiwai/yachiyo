package com.yuiwai.yachiyo.core

import utest._

object ColorSpec extends TestSuite {
  val tests = Tests {
    "RGB" - {
      "creation" - {
        val white = RGB(255, 255, 255)
        white ==> RGB.White

        val c = RGB(100, 10, 33)
        c.r ==> 100
        c.g ==> 10
        c.b ==> 33
      }
      "toHex" - {
        RGB.Black.toHexString ==> "000000"
        RGB.White.toHexString ==> "ffffff"
        RGB.Red.toHexString ==> "ff0000"
      }
      "invert" - {
        RGB.White.inverted ==> RGB.Black
        RGB(100, 33, 127).inverted ==> RGB(155, 222, 128)
      }
      "mix" - {
        RGB.White.mix(RGB.Black) ==> RGB(127, 127, 127)
      }
      "add" - {
      }
      "sub" - {
      }
      "multiply" - {
      }
    }
  }
}
