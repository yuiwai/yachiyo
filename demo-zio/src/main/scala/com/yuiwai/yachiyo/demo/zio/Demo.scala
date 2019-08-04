package com.yuiwai.yachiyo.demo.zio

import com.yuiwai.yachiyo.zio.ApplicationHandler
import zio.DefaultRuntime

object Demo extends DefaultRuntime {
  def main(args: Array[String]): Unit = {
    unsafeRun(ApplicationHandler.program())
  }
}
