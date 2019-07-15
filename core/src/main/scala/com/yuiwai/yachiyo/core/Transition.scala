package com.yuiwai.yachiyo.core

final case class Transition[A: Amount, P](
  initial: A,
  target: A,
  progress: Progress[P]
) {
  lazy val value: A = implicitly[Amount[A]].value(initial, target, progress.rate)
  def past(delta: P): Transition[A, P] = copy(progress = progress.past(delta))
  def withExtension(extension: RateExtension): Transition[A, P] = copy(progress = progress.withExtension(extension))
}

trait Plus[T] {
  def apply(l: T, r: T): T
}
object Plus {
  implicit val intPlus: Plus[Int] = _ + _
  implicit val doublePlus: Plus[Double] = _ + _
}
trait Multiply[T, V] {
  def apply(t: T, v: V): T
}
object Multiply {
  implicit val intDouble: Multiply[Int, Double] = { (i, d) => (i * d).toInt }
  implicit val intInt: Multiply[Int, Int] = _ * _
  implicit val doubleDouble: Multiply[Double, Double] = _ * _
}
trait Minus[T] {
  def apply(l: T, r: T): T
}
object Minus {
  implicit val intMinus: Minus[Int] = _ - _
  implicit val doubleMinus: Minus[Double] = _ - _
}
trait Amount[T] {
  def zero: T
  def value(initial: T, target: T, rate: Double): T
  def +(l: T, r: T): T
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
    def zero: Int = 0
    def toEachType(result: Double): Int = result.toInt
  }
  implicit val doubleAmount: Amount[Double] = new NumericAmount[Double] {
    def zero: Double = 0.0
    protected def toEachType(result: Double): Double = result
  }
  abstract class DelegateAmount[A, B: Amount] extends Amount[A] {
    private val amount = implicitly[Amount[B]]
    def zero: A = reverse(amount.zero)
    override def +(l: A, r: A): A = reverse(amount.+(convert(l), convert(r)))
    def convert(v: A): B
    def reverse(v: B): A
    def value(initial: A, target: A, rate: Double): A =
      reverse(amount.value(convert(initial), convert(target), rate))
  }
  implicit val charAmount: Amount[Char] = new DelegateAmount[Char, Int] {
    override def convert(v: Char): Int = v.toInt
    override def reverse(v: Int): Char = v.toChar
  }
  implicit def listAmount[T](implicit amount: Amount[T]): Amount[List[T]] = new Amount[List[T]] {
    def zero: List[T] = Nil
    override def +(l: List[T], r: List[T]): List[T] = l ++ r
    def value(initial: List[T], target: List[T], rate: Double): List[T] =
      initial.zip(target).map { case (i, t) => amount.value(i, t, rate) }
  }
  implicit val stringAmount: Amount[String] = new DelegateAmount[String, List[Char]] {
    override def convert(v: String): List[Char] = v.toList
    override def reverse(v: List[Char]): String = v.mkString
  }
  implicit def posAmount[T](implicit amount: Amount[T]): Amount[Pos[T]] = new Amount[Pos[T]] {
    def zero: Pos[T] = Pos(amount.zero, amount.zero)
    override def +(l: Pos[T], r: Pos[T]): Pos[T] = Pos(amount.+(l.x, r.x), amount.+(l.y, r.y))
    def value(initial: Pos[T], target: Pos[T], rate: Double): Pos[T] =
      Pos(amount.value(initial.x, target.x, rate), amount.value(initial.y, target.y, rate))
  }
  implicit val colorAmount: Amount[RGB] = new Amount[RGB] {
    def zero: RGB = RGB(0, 0, 0)
    override def +(l: RGB, r: RGB): RGB = RGB(l.r + r.r, l.g + r.g, l.b + r.b)
    def value(initial: RGB, target: RGB, rate: Double): RGB =
      RGB(
        intAmount.value(initial.r, target.r, rate),
        intAmount.value(initial.g, target.g, rate),
        intAmount.value(initial.b, target.b, rate)
      )
  }
}

final case class Progress[T: Counter](initial: T, target: T, current: T, extension: RateExtension = NoExtension) {
  private val counter = implicitly[Counter[T]]
  require(counter.<(initial, target) && counter.>=(current, initial) && counter.<=(current, target))
  lazy val rate: Double = extension.calculate(counter.rate(initial, target, current))
  def past(p: T): Progress[T] = copy(current = counter.+(current, p))
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
      numeric.toDouble(current) / (numeric.toDouble(target) - numeric.toDouble(initial))
  }
  implicit val intCounter: Counter[Int] = new NumericCounter[Int] {}
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
