package com.yuiwai.yachiyo.core

import scala.language.higherKinds


trait Mosaic[F[_], T] {
  def apply(data: F[T], size: Int): F[T]
}
object Mosaic {
  implicit def seqMosaic[T](implicit aggregation: Aggregation[Seq, T]): Mosaic[Seq, T] =
    new Mosaic[Seq, T] {
      override def apply(data: Seq[T], size: Int): Seq[T] =
        data.grouped(size).flatMap(l => Seq.fill(size)(aggregation.aggregate(l))).toSeq
    }

  def apply[F[_], T](data: F[T], size: Int)(implicit mosaic: Mosaic[F, T]): F[T] = mosaic(data, size)
}
