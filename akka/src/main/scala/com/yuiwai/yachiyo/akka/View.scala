package com.yuiwai.yachiyo.akka

trait View[M <: ViewModel] {
  def setup(viewModel: M): Unit
  def cleanup(): Unit
  def draw(viewModel: M): Unit
}
trait ViewModel
