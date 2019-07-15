package com.yuiwai.yachiyo.core

import utest._

object MatrixSpec extends TestSuite {
  val tests = Tests {
    val v1: Vec3[Int] = Vec3(1, 2, 3)
    val m1: Mat3[Int] = Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val m2: Mat3[Double] = Mat3(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
    "vec3" - {
      "add" - {
        v1 + Vec3.zero[Int] ==> v1
        v1 + v1 ==> Vec3(2, 4, 6)
      }
      "multiply" - {
        "scalar" - {
          v1 * 0 ==> Vec3.zero[Int]
          v1 * 2 ==> Vec3(2, 4, 6)
        }
        "vec" - {
          v1 * Vec3.zero[Int] ==> 0
          v1 * Vec3.unit[Int] ==> 6
          v1 * v1 ==> 14
        }
      }
    }
    "matrix3" - {
      "add" - {
        m1 + Mat3.zero ==> m1
        m1 + m1 ==> Mat3(2, 4, 6, 8, 10, 12, 14, 16, 18)

        m2 + Mat3.zero ==> m2
        m2 + m2 ==> Mat3(2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0)
      }
      "multiply" - {
        "row/col" - {
          m1.row1 ==> Vec3(1, 2, 3)
          m1.row2 ==> Vec3(4, 5, 6)
          m1.row3 ==> Vec3(7, 8, 9)
          m1.col1 ==> Vec3(1, 4, 7)
          m1.col2 ==> Vec3(2, 5, 8)
          m1.col3 ==> Vec3(3, 6, 9)
        }
        "scalar" - {
          m1 * 0 ==> Mat3.zero[Int]
          m1 * 2 ==> Mat3(2, 4, 6, 8, 10, 12, 14, 16, 18)
          m2 * 2 ==> Mat3(2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0)
        }
        "matrix" - {
          m1 * Mat3.zero[Int] ==> Mat3.zero[Int]
          m1 * Mat3.unit[Int] ==> m1
          m1 * Mat3.fill(1) ==> Mat3(6, 15, 24)

          m2 * Mat3.zero[Double] ==> Mat3.zero[Double]
          m2 * Mat3.unit[Double] ==> m2
          m2 * Mat3.fill(1.0) ==> Mat3(6.0, 15.0, 24.0)
        }
        "vec" - {
          m1 * Vec3.zero[Int] ==> Vec3.zero[Int]
          m1 * Vec3.unit[Int] ==> Vec3(6, 15, 24)
          m1 * v1 ==> Vec3(14, 32, 50)
        }
      }
    }
  }
}
