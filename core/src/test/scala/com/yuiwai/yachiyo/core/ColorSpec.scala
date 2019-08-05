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
      "map/map2" - {
        RGB.White.map(_ / 2) ==> RGB(127, 127, 127)
        RGB.White.map2(RGB.Black)(_ + _) ==> RGB.White
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
        RGB.Black + RGB.Black ==> RGB.Black
        RGB.White + RGB.White ==> RGB.White
        RGB(123, 100, 50) + RGB(123, 100, 250) ==> RGB(246, 200, 255)
      }
      "sub" - {
        RGB.White - RGB.White ==> RGB.Black
        RGB.White - RGB.Black ==> RGB.White
        RGB(123, 101, 50) - RGB(123, 100, 250) ==> RGB(0, 1, 0)
      }
      "multiply" - {
        RGB.White * RGB.White ==> RGB.White
        RGB.White * RGB.Black ==> RGB.Black
        RGB(100, 100, 100) * RGB(128, 255, 0) ==> RGB(50, 100, 0)
      }
      "max" - {
        RGB.White.max(RGB.Black) ==> RGB.White
        RGB(100, 20, 50).max(RGB(80, 90, 100)) ==> RGB(100, 90, 100)
      }
      "min" - {
        RGB.White.min(RGB.Black) ==> RGB.Black
        RGB(100, 20, 50).min(RGB(80, 90, 100)) ==> RGB(80, 20, 50)
      }
    }
  }
}
