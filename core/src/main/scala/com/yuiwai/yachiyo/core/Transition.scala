package com.yuiwai.yachiyo.core

final case class Transition[A: Amount, P: Counter](
  initial: A,
  target: A,
  progress: Progress[P]
) {
  lazy val value: A = implicitly[Amount[A]].value(initial, target, progress.rate)
  def past(delta: P): Transition[A, P] = copy(progress = progress.past(delta))
  def withNow(now: P): Transition[A, P] = copy(progress = progress.withNow(now))
  def withExtension(extension: RateExtension): Transition[A, P] = copy(progress = progress.withExtension(extension))
}

trait Amount[T] {
  def zero(implicit z: Zero[T]): T = z()
  def value(initial: T, target: T, rate: Double): T
  def +(l: T, r: T)(implicit plus: Plus[T]): T = plus(l, r)
}
object Amount {
  abstract class NumericAmount[T: Numeric] extends Amount[T] {
    private val numeric = implicitly[Numeric[T]]
    def +(l: T, r: T): T = numeric.plus(l, r)
    def value(initial: T, target: T, rate: Double): T =
      toEachType((numeric.toDouble(target) - numeric.toDouble(initial)) * rate + numeric.toDouble(initial))
    protected def toEachType(result: Double): T
  }
  implicit val intAmount: Amount[Int] = new NumericAmount[Int] {
    def toEachType(result: Double): Int = result.toInt
  }
  implicit val floatAmount: Amount[Float] = new NumericAmount[Float] {
    override protected def toEachType(result: Double): Float = result.toFloat
  }
  implicit val doubleAmount: Amount[Double] = new NumericAmount[Double] {
    protected def toEachType(result: Double): Double = result
  }
  abstract class DelegateAmount[A, B: Amount] extends Amount[A] {
    private val amount = implicitly[Amount[B]]
    def convert(v: A): B
    def reverse(v: B): A
    def value(initial: A, target: A, rate: Double): A =
      reverse(amount.value(convert(initial), convert(target), rate))
  }
  implicit val charAmount: Amount[Char] = new DelegateAmount[Char, Int] {
    override def convert(v: Char): Int = v.toInt
    override def reverse(v: Int): Char = v.toChar
  }
  implicit def listAmount[T](implicit amount: Amount[T]): Amount[List[T]] =
    (initial: List[T], target: List[T], rate: Double) =>
      initial.zip(target).map { case (i, t) => amount.value(i, t, rate) }
  implicit val stringAmount: Amount[String] = new DelegateAmount[String, List[Char]] {
    override def convert(v: String): List[Char] = v.toList
    override def reverse(v: List[Char]): String = v.mkString
  }
  implicit def posAmount[T](implicit amount: Amount[T]): Amount[Pos[T]] =
    (initial: Pos[T], target: Pos[T], rate: Double) =>
      Pos(amount.value(initial.x, target.x, rate), amount.value(initial.y, target.y, rate))
  implicit val colorAmount: Amount[RGB] = (initial: RGB, target: RGB, rate: Double) => RGB(
    intAmount.value(initial.r, target.r, rate),
    intAmount.value(initial.g, target.g, rate),
    intAmount.value(initial.b, target.b, rate)
  )
}

final case class Progress[T: Counter](initial: T, target: T, current: T, extension: RateExtension = NoExtension) {
  private val counter = implicitly[Counter[T]]
  require(counter.<(initial, target) && counter.>=(current, initial) && counter.<=(current, target))
  lazy val rate: Double = extension.calculate(counter.rate(initial, target, current))
  def past(p: T): Progress[T] = copy(current = counter.+(current, p))
  def withNow(p: T): Progress[T] = copy(current = if (counter.>(p, target)) target else p)
  def withExtension(extension: RateExtension): Progress[T] = copy(extension = extension)
}

abstract class Counter[T] {
  def +(l: T, r: T): T
  def <(l: T, r: T): Boolean
  def >(l: T, r: T): Boolean
  def >=(l: T, r: T): Boolean
  def <=(l: T, r: T): Boolean
  def rate(initial: T, target: T, current: T): Double
}
object Counter {
  abstract class NumericCounter[T: Numeric] extends Counter[T] {
    private val numeric = implicitly[Numeric[T]]
    def +(l: T, r: T): T = numeric.plus(l, r)
    def <(l: T, r: T): Boolean = numeric.lt(l, r)
    def >(l: T, r: T): Boolean = numeric.gt(l, r)
    def >=(l: T, r: T): Boolean = numeric.gteq(l, r)
    def <=(l: T, r: T): Boolean = numeric.lteq(l, r)
    def rate(initial: T, target: T, current: T): Double =
      (numeric.toDouble(current) - numeric.toDouble(initial)) / (numeric.toDouble(target) - numeric.toDouble(initial))
  }
  implicit val intCounter: Counter[Int] = new NumericCounter[Int] {}
  implicit val doubleCounter: Counter[Double] = new NumericCounter[Double] {}
}

trait RateExtension {
  def calculate(rate: Double): Double
}
object NoExtension extends RateExtension {
  override def calculate(rate: Double): Double = rate
}
object ReverseRateExtension extends RateExtension {
  override def calculate(rate: Double): Double = 1 - rate
}
object CycleRateExtension extends RateExtension {
  override def calculate(rate: Double): Double = if (rate > 0.5) (1 - rate) * 2 else rate * 2
}
object SinEaseInExtension extends RateExtension {
  override def calculate(rate: Double): Double = Math.sin(rate * 90 * Math.PI / 180)
}
object SinEaseOutExtension extends RateExtension {
  override def calculate(rate: Double): Double = 1 - Math.sin((1 - rate) * 90 * Math.PI / 180)
}
case class CompositeExtension(first: RateExtension, second: RateExtension) extends RateExtension {
  override def calculate(rate: Double): Double = second.calculate(first.calculate(rate))
}
