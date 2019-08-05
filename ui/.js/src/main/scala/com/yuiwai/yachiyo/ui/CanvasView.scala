package com.yuiwai.yachiyo.ui

import com.yuiwai.yachiyo.core.RGB
import org.scalajs.dom.raw.{HTMLCanvasElement, ImageData}

trait CanvasView extends DomView {
  def createCanvas(width: Int, height: Int): HTMLCanvasElement = {
    createElementAs[HTMLCanvasElement]("canvas", { e =>
      e.width = width
      e.height = height
    })
  }
}
object CanvasView {
  implicit class ImageDataWrap(imageData: ImageData) extends Block {
    override def width: Int = imageData.width
    override def height: Int = imageData.height
  }
  trait Block {
    def width: Int
    def height: Int
    def pixels: Seq[Pixel] = (0 until width * height) map (new Pixel(_))
  }
  final class Pixel(val index: Int) extends AnyVal {
    def get(imageData: ImageData): Option[RGB] = {
      val i = index * 4
      // TODO 領域外のindexの場合はNoneを返す
      Some(RGB(imageData.data(i), imageData.data(i + 1), imageData.data(i + 2)))
    }
    def put(imageData: ImageData, color: RGB): Unit = {
      val i = index * 4
      imageData.data(i) = color.r
      imageData.data(i + 1) = color.g
      imageData.data(i + 2) = color.b
    }
    def mod(imageData: ImageData, f: RGB => RGB): Unit =
      get(imageData).foreach(c => put(imageData, f(c)))
  }
}

