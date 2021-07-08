package com.yuiwai.yachiyo.ui

trait Presenter {
  type S <: Scene
  type M <: ViewModel
  type Prev = Option[M]
  def setup(initialState: S#State): M
  def updated(state: S#State): M => M
  def cleanup(): Unit = {}
}
