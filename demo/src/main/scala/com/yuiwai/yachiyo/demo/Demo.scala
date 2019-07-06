package com.yuiwai.yachiyo.demo

import akka.actor.typed.ActorSystem
import com.yuiwai.yachiyo.akka._

object Demo {
  def main(args: Array[String]): Unit = {
    ActorSystem(Application.behavior(DemoApplication), "demo-app")
  }
}

object DemoApplication extends Application {
  override def initialSceneSuite: SceneSuite = new SceneSuite {
    override val genScene: () => Scene = () => TopDemoScene
    override val genPresenter: () => Presenter = () => new TopPresenter
    override val genView: () => View = () => new TopView
  }
}
