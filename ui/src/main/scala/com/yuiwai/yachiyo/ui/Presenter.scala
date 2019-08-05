package com.yuiwai.yachiyo.ui

trait Presenter {
  type S <: Scene
  type M <: ViewModel
  type Prev = Option[M]
  def usePrevModel: Boolean = false
  def setup(initialState: S#State): M = updated(initialState, None)
  def updated(state: S#State, prevModel: Prev): M
  def cleanup(): Unit = {}
}
