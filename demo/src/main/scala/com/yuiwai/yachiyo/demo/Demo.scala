package com.yuiwai.yachiyo.demo

import akka.actor.typed.ActorSystem
import com.yuiwai.yachiyo.akka
import com.yuiwai.yachiyo.ui._

object Demo {
  def main(args: Array[String]): Unit = {
    ActorSystem(akka.Application.behavior(DemoApplication), "demo-app")
  }
}

object DemoApplication extends Application {
  override type SceneKey = Int
  val TopScene = 1
  override def initialSceneSuiteKey: Int = TopScene
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    TopScene -> new SceneSuite {
      override val genScene: () => Scene = () => TopDemoScene
      override val genPresenter: () => Presenter = () => new TopPresenter
      override val genView: () => View = () => new TopView
    }
  )
}
