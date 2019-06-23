package com.yuiwai.yachiyo.core

import utest._

object TransitionSpec extends TestSuite {
  val tests = Tests {
    "transition" - {
      "int" - {
        val t = Transition(0, 10, Progress(0, 100, 0))
        t.value ==> 0
        t.past(100).value ==> 10
        t.past(50).value ==> 5
        t.past(41).value ==> 4
      }
      "int reduce" - {
        val t = Transition(10, 0, Progress(0, 100, 0))
        t.value ==> 10
        t.past(100).value ==> 0
        t.past(50).value ==> 5
        t.past(41).value ==> 5
      }
      "double" - {
        val t = Transition(0.0, 10.0, Progress(0, 100, 0))
        t.value ==> 0.0
        t.past(100).value ==> 10.0
        t.past(50).value ==> 5.0
        t.past(41).value ==> 4.1
      }
      "char" - {
        val t = Transition('a', 'c', Progress(0, 100, 0))
        t.value ==> 'a'
        t.past(100).value ==> 'c'
        t.past(50).value ==> 'b'
        t.past(41).value ==> 'a'

        val j = Transition('あ', 'ん', Progress(0, 100, 0))
        j.value ==> 'あ'
        j.past(100).value ==> 'ん'
        j.past(50).value ==> 'な'
        j.past(41).value ==> 'っ'
      }
      "list of int" - {
        val t = Transition(List(0, 0, 0), List(10, 50, 100), Progress(0, 100, 0))
        t.value ==> List(0, 0, 0)
        t.past(100).value ==> List(10, 50, 100)
        t.past(50).value ==> List(5, 25, 50)
        t.past(41).value ==> List(4, 20, 41)
      }
      "string" - {
        val t = Transition("foo", "bar", Progress(0, 100, 0))
        t.value ==> "foo"
        t.past(100).value ==> "bar"
        t.past(50).value ==> "dhp"
        t.past(41).value ==> "dip"

        val j = Transition("たろう", "はなこ", Progress(0, 100, 0))
        j.value ==> "たろう"
        j.past(100).value ==> "はなこ"
        j.past(50).value ==> "でほが"
        j.past(41).value ==> "づまか"
      }
      "pos" - {
        val t = Transition(Pos(0, 0), Pos(50, 100), Progress(0, 100, 0))
        t.value ==> Pos(0, 0)
        t.past(100).value ==> Pos(50, 100)
        t.past(50).value ==> Pos(25, 50)
        t.past(41).value ==> Pos(20, 41)
      }
    }
  }
}

object ProgressSpec extends TestSuite {
  val tests = Tests {
    "rate" - {
      Progress(0, 1, 0).rate ==> 0
      Progress(0, 1, 1).rate ==> 1
      Progress(0, 2, 1).rate ==> 0.5
    }
    "past" - {
      val p = Progress(0, 100, 0)
      p.past(10).current ==> 10
      p.past(10).past(10).current ==> 20
      intercept[IllegalArgumentException](p.past(101))
    }
    "extension" - {
      "reverse" - {
        Progress(0, 1, 0, ReverseRateExtension).rate ==> 1
        Progress(0, 1, 1, ReverseRateExtension).rate ==> 0
        Progress(0, 2, 1, ReverseRateExtension).rate ==> 0.5
      }
      "sin-ease-in" - {
        Progress(0, 1, 0, SinEaseInExtension).rate ==> 0
        Progress(0, 1, 1, SinEaseInExtension).rate ==> 1
        assert(Progress(0, 2, 1, SinEaseInExtension).rate > 0.5)
      }
      "sin-ease-out" - {
        Progress(0, 1, 0, SinEaseOutExtension).rate ==> 0
        Progress(0, 1, 1, SinEaseOutExtension).rate ==> 1
        assert(Progress(0, 2, 1, SinEaseOutExtension).rate < 0.5)
      }
    }
  }
}