package com.yuiwai.yachiyo.demo

import akka.actor.typed.ActorSystem
import com.yuiwai.yachiyo.akka.{Application, Scene}

object Demo {
  def main(args: Array[String]): Unit = {
    ActorSystem(Application.behavior(DemoApplication), "demo-app")
  }
}

object DemoApplication extends Application {
  override val initialScene: Scene = TopDemoScene
}
