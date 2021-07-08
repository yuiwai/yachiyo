package com.yuiwai.yachiyo.ui

trait View {
  type M <: ViewModel
  type S <: Scene
  type Listener = S#Command => Unit
  def setup(viewModel: M, listener: Listener): Unit
  def update(viewModelMod: M => M): Unit
  def cleanup(): Unit
}

trait ViewModel
