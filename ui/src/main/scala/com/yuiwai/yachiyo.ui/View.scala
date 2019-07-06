package com.yuiwai.yachiyo.ui

trait View {
  type M <: ViewModel
  type S <: Scene
  type Listener = S#Command => Unit
  def setup(viewModel: M, listener: Listener): Unit
  def cleanup(): Unit
  def draw(viewModel: M): Unit
}

trait ViewModel
