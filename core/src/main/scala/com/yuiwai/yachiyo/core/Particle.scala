package com.yuiwai.yachiyo.core

final case class Particle[T: Amount](pos: Pos[T], speed: Speed[T], lifetime: Int = 0) {
  def accelerated(v: Force[T]): Particle[T] = copy(speed = speed + v)
  def updated(): Particle[T] = copy(pos = pos + speed, lifetime = lifetime + 1)
}
object Particle {
  def zero[T: Plus : Minus](implicit amount: Amount[T], multiply: Multiply[T, Double]): Particle[T] =
    apply(Pos.zero, Speed.zero, 0)
}

final case class ParticleSystem[T: Amount : Plus : Minus](
  pos: Pos[T],
  lifetime: Int,
  particles: Seq[Particle[T]],
  generator: Generator[ParticleSystem[T], Particle[T]])
  (implicit multiply: Multiply[T, Double]) {
  def size: Int = particles.size
  def spawn(speed: Speed[T] = Speed.zero): Particle[T] = Particle(pos, speed)
  def updated(): ParticleSystem[T] = generator.generate(this) match {
    case (generated, gen) =>
      copy(
        particles = (particles.map(_.updated()) ++ generated).filter(_.lifetime < lifetime),
        generator = gen
      )
  }
}

// TODO 頻度の定義は要調整
final case class Generator[C, T](gen: C => T, frequency: Int = 1, counter: Int = 0) {
  def generate(context: C, generated: Seq[T] = Seq.empty): (Seq[T], Generator[C, T]) = {
    if (counter >= frequency) copy(counter = counter - frequency).generate(context, generated :+ gen(context))
    else (generated, copy(counter = counter + 1))
  }
}

final case class Gravity(value: Double) extends AnyVal

final case class Speed[T: Amount](x: T, y: T)
  (implicit multiply: Multiply[T, Double], minus: Minus[T], plus: Plus[T]) extends Vector2D[T] {
  def +(that: Force[T]): Speed[T] = copy(amount.+(x, that.x), amount.+(y, that.y))
  def *(angle: Angle): Speed[T] =
    copy(
      minus(multiply(x, angle.cos), multiply(y, angle.sin)),
      plus(multiply(x, angle.sin), multiply(y, angle.cos))
    )
}
object Speed {
  def zero[T: Plus : Minus](implicit amount: Amount[T], multiply: Multiply[T, Double]): Speed[T] =
    apply(amount.zero, amount.zero)
}
final case class Force[T: Amount](x: T, y: T) extends Vector2D[T]
abstract class Vector2D[T](implicit val amount: Amount[T]) {
  val x: T
  val y: T
}

final case class Angle(value: Double) {
  lazy val cos: Double = Math.cos(value)
  lazy val sin: Double = Math.sin(value)
}
object Angle {
  def fromDegree(deg: Int): Angle = apply(deg * Math.PI / 180)
  def random(fromDeg: Int, toDeg: Int): Angle = fromDegree(fromDeg + ((toDeg - fromDeg) * Math.random()).toInt)
}

// TODO impl
trait Steering {

}