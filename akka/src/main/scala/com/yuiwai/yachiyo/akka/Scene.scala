package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.{CleanedUp, Initialized, SceneCallback, StateChangedCallback}

object Scene {
  type GenScene = () => ui.Scene
  sealed trait SceneCommand[-T]
  final case class Initialize(genScene: GenScene) extends SceneCommand[Nothing]
  case object CleanUp extends SceneCommand[Nothing]
  case object Start extends SceneCommand[Nothing]
  case object Stop extends SceneCommand[Nothing]
  final case class Execution[S <: ui.Scene](input: S#Command) extends SceneCommand[S]

  private def deployed(scene: ui.Scene, listener: SceneCallback => Unit): Behavior[SceneCommand[_]] = {
    def make(state: scene.State): Behavior[SceneCommand[_]] = {
      listener(StateChangedCallback(state))
      Behaviors.receive[SceneCommand[_]] { (_, msg) =>
        msg match {
          case Execution(input) =>
            val (newState, event, output) = scene.execute(state, input.asInstanceOf[scene.Command])
            listener(output)
            make(newState)
          case CleanUp =>
            scene.cleanup()
            listener(CleanedUp)
            init(listener)
          case _ => Behaviors.same
        }
      }
    }
    {
      val state = scene.initialState()
      listener(Initialized(state))
      make(state)
    }
  }
  def init(listener: SceneCallback => Unit): Behavior[SceneCommand[_]] =
    Behaviors.receive[SceneCommand[_]] { (_, msg) =>
      msg match {
        case Initialize(genScene) => deployed(genScene(), listener)
        case _ => Behaviors.same
      }
    }
}
