package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Scene.SceneCallback

trait Scene {
  type State
  type Command
  type Event
  def initialState(/* TODO GlobalState */): State
  def presenter(): Presenter[this.type] // FIXME ApplicationがSceneとPresenterの組み合わせを受け持つ?
  def execute(state: State, input: Command): (State, Event, SceneCallback)
  def cleanup(): Unit
}
object Scene {
  sealed trait SceneCommand[-T]
  final case class Initialize(scene: Scene) extends SceneCommand[Nothing]
  case object Start extends SceneCommand[Nothing]
  case object Stop extends SceneCommand[Nothing]
  final case class Execution[S <: Scene](input: S#Command) extends SceneCommand[S]
  final case class ChangeScene[S <: Scene](scene: S) extends SceneCommand[S]

  sealed trait SceneCallback
  case object NoCallback extends SceneCallback
  final case class Initialized[S <: Scene](state: S#State, presenter: Presenter[S]) extends SceneCallback
  final case class EventCallback(event: Scene#Event) extends SceneCallback
  final case class NextSceneCallback[S <: Scene](genScene: () => S) extends SceneCallback
  final case class CurrentState(state: Scene#State) extends SceneCallback

  private def deployed(scene: Scene, listener: SceneCallback => Unit): Behavior[SceneCommand[_]] = {
    def make(state: scene.State): Behavior[SceneCommand[_]] = {
      listener(CurrentState(state))
      Behaviors.receive[SceneCommand[_]] { (_, msg) =>
        msg match {
          case Execution(input) =>
            val (newState, event, output) = scene.execute(state, input.asInstanceOf[scene.Command])
            listener(output)
            make(newState)
          case ChangeScene(newScene) =>
            // TODO cleanupはここではない
            scene.cleanup()
            deployed(newScene, listener)
          case _ =>
            // TODO other messages.
            Behaviors.same
        }
      }
    }
    {
      val state = scene.initialState()
      listener(Initialized(state, scene.presenter()))
      make(scene.initialState())
    }
  }
  def init(listener: SceneCallback => Unit): Behavior[SceneCommand[_]] =
    Behaviors.receive[SceneCommand[_]] { (_, msg) =>
      msg match {
        case Initialize(scene) => deployed(scene, listener)
        case _ => Behaviors.same
      }
    }
}
