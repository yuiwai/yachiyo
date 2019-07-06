package com.yuiwai.yachiyo.core

final case class Particle[T: Amount](pos: Pos[T])
final case class ParticleSystem[T: Amount](pos: Pos[T], particles: Seq[Particle[T]]) {

}

final case class Gravity(value: Double) extends AnyVal

