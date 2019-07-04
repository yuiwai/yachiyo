package com.yuiwai.yachiyo.core

final case class Particle[T: Amount](pos: Pos[T])

final case class Gravity(value: Double) extends AnyVal

