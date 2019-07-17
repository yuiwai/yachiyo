package com.yuiwai.yachiyo.core

trait Block[T] {
  def width: Int
  def height: Int
  def size: Int = values.size
  val values: Seq[T]
  def iterator: BlockIterator[T] = new BlockIterator[T](this, 0)
  def foreach(f: T => Unit): Unit = values.foreach(f)
}
final case class BlockImpl[T](width: Int, height: Int, values: Seq[T]) extends Block[T]

class BlockIterator[T](block: Block[T], pos: Int) {
  def hasNext: Boolean = pos < block.size
  def next: BlockIterator[T] = new BlockIterator(block, pos + 1)
  def value: T = block.values(pos)
}

object Block {
  def empty[T]: Block[T] = BlockImpl(0, 0, Seq.empty)
  def fill[T](width: Int, height: Int, value: => T): Block[T] = BlockImpl(width, height, Seq.fill(width * height)(value))
  def fill[T](width: Int, value: => T): Block[T] = fill(width, width, value)
}

