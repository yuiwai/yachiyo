package com.yuiwai.yachiyo.ui

trait Presenter {
  type S <: Scene
  type M <: ViewModel
  def setup(initialState: S#State): M = updated(initialState)
  def cleanup(): Unit = {}
  def updated(state: S#State): M
}
