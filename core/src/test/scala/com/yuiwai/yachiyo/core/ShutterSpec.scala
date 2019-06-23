package com.yuiwai.yachiyo.core

import utest._

object ShutterSpec extends TestSuite {
  val tests = Tests {
    "bounded" - {
      "int" - {
        val b = new ScalarBound[Int](10, 20) {}
        b.contain(Int.MinValue) ==> false
        b.contain(10) ==> false
        b.contain(11) ==> true
        b.contain(19) ==> true
        b.contain(20) ==> false
        b.contain(Int.MaxValue) ==> false
      }
      "pos" - {
        case class P(x: Int, y: Int)
        val b = new RectangleBound[P, Int](_.x, _.y, P(3, 3), P(5, 5)) {}
        b.contain(P(3, 3)) ==> false
        b.contain(P(4, 3)) ==> false
        b.contain(P(3, 4)) ==> false
        b.contain(P(4, 4)) ==> true
        b.contain(P(5, 4)) ==> false
        b.contain(P(4, 5)) ==> false
        b.contain(P(5, 5)) ==> false
      }
    }
    "shutter" - {
      "int list" - {
        val subject = List(0, 1, 2, 3, 4, 5, 6, 7, 8)
        implicit val lens: Lens[List, Int, Int] = {
          (scale, center) => { i =>
            val s = scale.value
            Some(i).filter(j => j >= center - 2 * s && j <= center + 2 * s && j % s == 0)
          }
        }
        implicit val sensor: Sensor[List, Int, Int, Int] = { (sight, subject) =>
          subject.zipWithIndex.flatMap { case (v, i) => sight.collect(i).map(_ => v) }
        }
        Shutter.release(Scale(1))(4)(subject) ==> List(2, 3, 4, 5, 6)
        Shutter.release(Scale(2))(4)(subject) ==> List(0, 2, 4, 6, 8)
        Shutter.release(Scale(3))(4)(subject) ==> List(0, 3, 6)
      }
      "set of pos" - {
        case class V(p: P, value: Int)
        case class P(x: Int, y: Int) {
          def dist(that: P): Int = {
            val xd = x - that.x
            val yd = y - that.y
            xd * xd + yd * yd
          }
        }
        val subject: Set[V] = Set(
          V(P(3, 5), 12), V(P(1, 5), 87), V(P(2, 3), 19), V(P(5, 4), 85), V(P(3, 3), 40), V(P(2, 1), 53), V(P(1, 4), 22)
        )
        implicit val lens: Lens[Set, Int, P] = {
          (scale, center) => { p =>
            val s = scale.value
            if (center.dist(p) > s * s) None
            else Some(p)
          }
        }
        implicit val sensor: Sensor[Set, P, V, Int] = { (sight, subject) =>
          subject.flatMap { v => sight.collect(v.p).map(_ => v.value) }
        }
        Shutter.release(Scale(1))(P(3, 3))(subject) ==> Set(40, 19)
        Shutter.release(Scale(2))(P(3, 3))(subject) ==> Set(12, 40, 19)
        Shutter.release(Scale(3))(P(3, 3))(subject) ==> Set(85, 53, 22, 12, 87, 40, 19)
      }
    }
  }
}
