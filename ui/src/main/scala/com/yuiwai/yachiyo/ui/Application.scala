package com.yuiwai.yachiyo.ui

trait Application {
  val sceneSuiteMap: Map[Int, SceneSuite]
  def initialSceneSuiteKey: Int
  // TODO 存在しないキーの場合のフォロー
  def resolve(sceneKey: Int): SceneSuite = sceneSuiteMap(sceneKey)
  def initialSceneSuite: SceneSuite = resolve(initialSceneSuiteKey)
}

case class SceneSuite(
  genScene: () => Scene,
  genPresenter: () => Presenter,
  genView: () => View
)
