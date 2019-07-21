package com.yuiwai.yachiyo.core

trait Block[T] {
  type P = Pos[Int]
  def width: Int
  def height: Int
  def size: Int = values.size
  def posToIndex(pos: P): Option[Int] = Some(pos.x + pos.y * width).filter(_ < size)
  def indexToPos(index: Int): P = Pos(index % width, index / width)
  val values: Seq[T]
  def valuesWithPos: Seq[(P, T)] = values.zipWithIndex.map { case (v, i) => indexToPos(i) -> v }
  def rows: Seq[Seq[T]] = values.grouped(width).toSeq
  def map[U](f: T => U): Block[U]
  def mapRow[U](f: Seq[T] => Seq[U]): Option[Block[U]]
  def iterator: BlockIterator[T] = new BlockIterator[T](this, 0)
  def foreach(f: T => Unit): Unit = values.foreach(f)
  def row(index: Int): Option[Seq[T]]
  def region(from: P, width: Int, height: Int): Region[T]
  def updated(pos: P, value: T): Block[T]
  def patch(dest: P, block: Block[T]): Block[T] = block.valuesWithPos.foldLeft(this) {
    case (acc, (p, v)) => acc.updated(dest + p, v)
  }
  def concatX(that: Block[T]): Option[Block[T]]
  def concatY(that: Block[T]): Option[Block[T]]
  def flipX: Block[T]
  def flipY: Block[T]
  def find(f: T => Boolean): Option[T]
  def rotateR: Block[T]
  def rotateL: Block[T]
  def resizeTo(w: Int, h: Int)
    (implicit
      plus: Plus[T],
      minus: Minus[T],
      multiply: Multiply[T, T],
      divide: Divide[T, T],
      zero: Zero[T],
      unit: UNIT[T],
      fromDouble: FromDouble[T]
    ): Option[Block[T]]
  def +(that: Block[T])(implicit plus: Plus[T]): Option[Block[T]]
}
case class BlockImpl[T](width: Int, height: Int, values: Seq[T]) extends Block[T] {
  override def map[U](f: T => U): Block[U] = BlockImpl(width, height, values.map(f))
  override def mapRow[U](f: Seq[T] => Seq[U]): Option[Block[U]] = {
    val rows = values.grouped(width).map(f).toSeq
    val w = rows.head.size
    if (!rows.forall(_.length == w)) None
    else Some(Block.withValues(w, rows.flatten))
  }
  override def row(index: Int): Option[Seq[T]] =
    if (index < 0 || index >= height) None
    else Some(values.slice(index * width, (index + 1) * width))
  override def region(from: P, width: Int, height: Int): Region[T] =
    new RegionImpl(this, from, width, height)
  override def updated(pos: P, value: T): Block[T] =
    posToIndex(pos).map(i => copy(values = values.updated(i, value))).getOrElse(this)
  override def concatX(that: Block[T]): Option[Block[T]] =
    if (height != that.height) None
    else Some {
      copy(
        width = width + that.width,
        values = values.grouped(width).zip(that.values.grouped(that.width)).flatMap(xs => xs._1 ++ xs._2).toList
      )
    }
  override def concatY(that: Block[T]): Option[Block[T]] =
    if (width != that.width) None
    else Some(copy(height = height + that.height, values = values ++ that.values))
  override def flipX: Block[T] = copy(values = values.grouped(width).flatMap(_.reverse).toList)
  override def flipY: Block[T] = copy(values = values.grouped(width).toList.reverse.flatten)
  override def find(f: T => Boolean): Option[T] = values.find(f)
  override def rotateR: Block[T] =
    copy(values = for {x <- 0 until width; y <- 1 to height} yield values(x + (height - y) * width))
  override def rotateL: Block[T] =
    copy(values = for {x <- 1 to width; y <- 0 until height} yield values(width - x + y * width))
  override def resizeTo(w: Int, h: Int)
    (implicit
      plus: Plus[T],
      minus: Minus[T],
      multiply: Multiply[T, T],
      divide: Divide[T, T],
      zero: Zero[T],
      unit: UNIT[T],
      fromDouble: FromDouble[T]
    ): Option[Block[T]] =
    if (w == width && h == height) Some(this)
    else {
      if (w != width) {
        mapRow[T](row => Block.resizeX(w, 0, row))
      } else Some(this)
    }
  override def +(that: Block[T])(implicit plus: Plus[T]): Option[Block[T]] =
    if (width != that.width || height != that.height) None
    else Some(copy(values = values.zip(that.values).map(t => plus(t._1, t._2))))
}

class BlockIterator[T](block: Block[T], pos: Int) {
  def hasNext: Boolean = pos < block.size
  def next: BlockIterator[T] = new BlockIterator(block, pos + 1)
  def value: T = block.values(pos)
}

object Block {
  def empty[T]: Block[T] = BlockImpl(0, 0, Seq.empty)
  def fill[T](width: Int, height: Int, value: => T): Block[T] = BlockImpl(width, height, Seq.fill(width * height)(value))
  def fillZero[T](width: Int, height: Int)(implicit zero: Zero[T]): Block[T] = fill(width, height, zero())
  def fillSquare[T](width: Int, value: => T): Block[T] = fill(width, width, value)
  def fillWithIndex[T](width: Int, height: Int)(gen: Int => T): Block[T] =
    BlockImpl(width, height, Seq.tabulate(width * height)(gen))
  def fillWithPos[T](width: Int, height: Int)(gen: Pos[Int] => T): Block[T] =
    fillWithIndex(width, height) { i => gen(Pos(i % width, i / width)) }
  def fillWithDistance[T](width: Int, height: Int)(gen: Double => T): Block[T] =
    fillWithPos(width, height)(p => gen(Math.sqrt((p.x + .5) * (p.x + .5) + (p.y + .5) * (p.y + .5))))
  def withValues[T](width: Int, values: Seq[T]): Block[T] = new BlockImpl[T](width, values.size / width, values)
  def resizeX[T: Plus : Minus](width: Int, current: Int, source: Seq[T], dest: Seq[T] = Seq.empty)
    (implicit
      zero: Zero[T],
      unit: UNIT[T],
      fromDouble: FromDouble[T],
      divide: Divide[T, T],
      multiply: Multiply[T, T]): Seq[T] =
    if (width <= 0 || source.isEmpty) Seq.empty
    else if (current < width) {
      if (source.size == 1) Seq.fill(width)(source.head)
      else if (dest.isEmpty) resizeX(width, current + 1, source, dest :+ source.head)
      else if (current + 1 == width) resizeX(width, current + 1, source, dest :+ source.last)
      else {
        val xd = current.toDouble * (source.size - 1) / (width - 1)
        val xi = xd.toInt
        resizeX(
          width,
          current + 1,
          source,
          dest :+ Linear(zero(), source(xi), unit(), source(xi + 1))
            .apply(fromDouble(xd - xi)))
      }
    }
    else dest

  implicit class BitBlockAdapter(block: Block[Boolean]) extends BitBlock(block)
}

trait Region[T] {
  def size: Int
}
final class RegionImpl[T](block: Block[T], from: Pos[Int], width: Int, height: Int) extends Region[T] {
  override def size: Int = width.min(block.width - from.x + 1) * height.min(block.height - from.y + 1)
}

class BitBlock(self: Block[Boolean]) {
  def mask[T](block: Block[T])(implicit zero: Zero[T]): Block[T] =
    Block.withValues(self.width.min(block.width), block.values.zip(self.values).map {
      case (t, true) => t
      case (_, false) => zero()
    })
}
