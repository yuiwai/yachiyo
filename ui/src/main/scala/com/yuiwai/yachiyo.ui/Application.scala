package com.yuiwai.yachiyo.ui

trait Application {
  type SceneKey
  val sceneSuiteMap: Map[SceneKey, SceneSuite]
  def initialSceneSuiteKey: SceneKey
  def initialSceneSuite: SceneSuite = sceneSuiteMap(initialSceneSuiteKey)
}

abstract class SceneSuite {
  val genScene: () => Scene
  val genPresenter: () => Presenter
  val genView: () => View
}
