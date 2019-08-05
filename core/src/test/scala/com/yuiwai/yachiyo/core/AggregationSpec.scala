package com.yuiwai.yachiyo.core

import utest._

object AggregationSpec extends TestSuite {
  val tests = Tests {
    "intList" - {
      implicit def listOrd[T]: Ordering[List[T]] = (x: List[_], y: List[_]) => x.size compare y.size

      Aggregation.sum(Seq(1, 2, 3, 4)) ==> 10
      Aggregation.avg(Seq(1.0, 2.0, 3.0, 4.0)) ==> 2.5
      Aggregation.mid(Seq('a', 'b', 'c')) ==> 'b'
      Aggregation.max(Seq(List(1), List(1, 2), Nil)) ==> List(1, 2)
      Aggregation.min(Seq("foo", "bar", "baz")) ==> "bar"
    }
  }
}
