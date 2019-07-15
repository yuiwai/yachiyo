package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.ui.DomView

trait CommonScene {

}

trait CommonView {
  self: DomView =>
  protected def container = elementById("container")
  override def cleanup(): Unit = {
    container.innerHTML = ""
  }
}
