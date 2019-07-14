package com.yuiwai.yachiyo.ui

import org.scalajs.dom
import org.scalajs.dom.raw._

trait SvgView extends DomView {
  var xmlns = "http://www.w3.org/2000/svg"
  def svg(): SVGSVGElement = createElementNS("svg").asInstanceOf[SVGSVGElement]
  def group(elems: SVGElement*): SVGGElement = createElementNS("g").asInstanceOf[SVGGElement]
    .tap { e =>
      elems foreach e.appendChild
    }
  def text(value: String): SVGTextElement = createElementNS("text").asInstanceOf[SVGTextElement]
    .tap { e =>
      e.textContent = value
    }
  def line(): SVGLineElement = createElementNS("line").asInstanceOf[SVGLineElement]
  def createElementNS(name: String): SVGElement = dom.document.createElementNS(xmlns, name).asInstanceOf[SVGElement]

  implicit class SVGElementWrap[E <: SVGElement](element: E) {
    def tap(f: E => Unit): E = {
      f(element)
      element
    }
  }
}
