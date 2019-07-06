package com.yuiwai.yachiyo.akka

import com.yuiwai.yachiyo.ui
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLElement}

trait DomView extends ui.View {
  def createElement(name: String, tap: HTMLElement => Unit = { _ => () }): HTMLElement = {
    val e = dom.document.createElement(name).asInstanceOf[HTMLElement]
    tap(e)
    e
  }
  def createElementAs[E <: HTMLElement](name: String, tap: E => Unit = { _: E => () }): E = {
    val e = createElement(name).asInstanceOf[E]
    tap(e)
    e
  }
  def elementById(id: String): HTMLElement = dom.document.getElementById(id).asInstanceOf[HTMLElement]
  def button(text: String): HTMLButtonElement = createElementAs[HTMLButtonElement]("button", _.innerText = text)
}
