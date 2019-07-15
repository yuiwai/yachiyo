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
  val TopSceneKey = 1
  val TransitionSceneKey = 2
  val ParticleSceneKey = 3
  val NodeSceneKey = 4
  override def initialSceneSuiteKey: Int = TopSceneKey
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    TopSceneKey -> SceneSuite(
      () => TopDemoScene,
      () => new TopPresenter,
      () => new TopView
    ),
    TransitionSceneKey -> SceneSuite(
      () => TransitionDemoScene,
      () => new TransitionDemoPresenter,
      () => new TransitionView
    ),
    ParticleSceneKey -> SceneSuite(
      () => ParticleDemoScene,
      () => new ParticleDemoPresenter,
      () => new ParticleDemoView
    ),
    NodeSceneKey -> SceneSuite(
      () => NodeDemoScene,
      () => new NodeDemoPresenter,
      () => new NodeDemoView
    )
  )
}
