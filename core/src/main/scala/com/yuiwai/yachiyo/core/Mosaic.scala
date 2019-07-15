package com.yuiwai.yachiyo.core

import scala.language.higherKinds


trait Mosaic[F[_], T] {
  def apply(data: F[T], size: Int): F[T]
}
object Mosaic {
  implicit def seqMosaic[T](implicit aggregation: Aggregation[Seq, T]): Mosaic[Seq, T] =
    (data: Seq[T], size: Int) => data.grouped(size).flatMap(l => Seq.fill(size)(aggregation.aggregate(l))).toSeq
  def apply[F[_], T](data: F[T], size: Int)(implicit mosaic: Mosaic[F, T], aggregation: Aggregation[F, T]): F[T] =
    mosaic(data, size)
}
