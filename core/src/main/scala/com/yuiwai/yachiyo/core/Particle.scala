package com.yuiwai.yachiyo.core

final case class Particle[T: Amount](pos: Pos[T], speed: Speed[T], lifetime: Int = 0) {
  def accelerated(v: Force[T]): Particle[T] = copy(speed = speed + v)
  def updated()(implicit plus: Plus[T]): Particle[T] = copy(pos = pos + speed, lifetime = lifetime + 1)
}
object Particle {
  def zero[T: Plus : Minus : Zero](implicit amount: Amount[T], multiply: Multiply[T, Double]): Particle[T] =
    apply(Pos.zero, Speed.zero)
}

final case class ParticleSystem[T: Amount : Plus : Minus : Zero](
  pos: Pos[T],
  lifetime: Int,
  particles: Seq[Particle[T]],
  generator: Generator[ParticleSystem[T], Particle[T]],
  gravity: Gravity[T])
  (implicit multiply: Multiply[T, Double]) {
  def size: Int = particles.size
  def spawn(speed: Speed[T] = Speed.zero): Particle[T] = Particle(pos, speed)
  def updated(): ParticleSystem[T] = generator.generate(this) match {
    case (generated, gen) =>
      copy(
        particles = (particles.map(_.accelerated(gravity.force).updated()) ++ generated).filter(_.lifetime < lifetime),
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

final case class Gravity[T](force: Force[T]) {
  def apply(speed: Speed[T]): Speed[T] = speed + force
}
object Gravity {
  def zero[T: Amount : Zero]: Gravity[T] = apply(Force.zero[T])
  def apply[T: Amount](value: T, angle: Angle)(implicit multiply: Multiply[T, Double]): Gravity[T] =
    apply(Force(multiply(value, angle.cos), multiply(value, angle.sin)))
}

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
  def zero[T: Plus : Minus : Zero](implicit amount: Amount[T], multiply: Multiply[T, Double]): Speed[T] =
    apply(amount.zero, amount.zero)
}
final case class Force[T: Amount](x: T, y: T) extends Vector2D[T]
object Force {
  def zero[T: Zero](implicit amount: Amount[T]): Force[T] = apply(amount.zero, amount.zero)
}
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
  def down: Angle = fromDegree(270)
  def up: Angle = fromDegree(90)
}

// TODO impl
trait Steering {

}