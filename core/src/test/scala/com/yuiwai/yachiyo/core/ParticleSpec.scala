package com.yuiwai.yachiyo.core

import utest._

object ParticleSpec extends TestSuite {
  val tests = Tests {
    val system = ParticleSystem[Int](
      Pos.zero,
      0,
      Seq.empty,
      Generator(_ => Particle.zero[Int], 10, 1))
    "spawn" - {
      system.spawn().pos ==> Pos.zero[Int]
    }
    "generator" - {
      Generator[Unit, Int](_ => 1, 1, 0).generate(()) match {
        case (generated, _) => generated.size ==> 0
      }
      Generator[Unit, Int](_ => 1, 1, 10).generate(()) match {
        case (generated, _) => generated.size ==> 10
      }
    }
    "accelerated" - {
      val p1 = Particle.zero[Int]
      p1.speed ==> Speed.zero[Int]
      p1.accelerated(Force(1, 2)).updated().speed ==> Speed(1, 2)
    }
    "updated" - {
      val p1 = Particle[Int](Pos.zero, Speed.zero, 0)
      p1.accelerated(Force(1, 2)).updated() match {
        case Particle(p, _, _) => p ==> Pos(1, 2)
      }
    }
    "angle" - {
      Speed(1, 1) * Angle.fromDegree(0) ==> Speed(1, 1)
      Speed(1, 0) * Angle.fromDegree(90) ==> Speed(0, 1)
    }
    "lifetime" - {
      val p1 = Particle[Int](Pos.zero, Speed.zero, 0)
      p1.updated() match {
        case Particle(_, _, lifetime) => lifetime ==> 1
      }
    }
  }
}
