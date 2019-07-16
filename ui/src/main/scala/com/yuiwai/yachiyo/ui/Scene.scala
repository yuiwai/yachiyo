package com.yuiwai.yachiyo.ui

trait Scene {
  type State
  type Command
  type Event
  type Result = (State, Event, SceneCallback)
  def initialState(/* TODO GlobalState */): State
  def execute(state: State, input: Command): Result
  def cleanup(): Unit = {}
}

sealed trait SceneCallback
case object NoCallback extends SceneCallback
final case class Initialized[S <: Scene](initialState: S#State) extends SceneCallback
case object CleanedUp extends SceneCallback
final case class EventCallback(event: Scene#Event) extends SceneCallback
final case class NextSceneCallback(sceneKey: Int) extends SceneCallback
final case class StateChangedCallback(state: Scene#State) extends SceneCallback
