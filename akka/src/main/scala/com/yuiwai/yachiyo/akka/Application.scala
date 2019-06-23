package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Scene.SceneCallback

trait Application {
  protected def initialScene: Scene
}
object Application {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  def behavior(application: Application): Behavior[ApplicationCommand] = Behaviors.setup { ctx =>
    val sceneRef = ctx.spawn(Scene.init(ctx.self ! SceneCallbackWrap(_)), "scene")
    val presenterRef = ctx.spawn(Presenter.init(_ => ()), "presenter")

    sceneRef ! Scene.Initialize(application.initialScene)

    Behaviors.receiveMessage[ApplicationCommand] {
      case SceneCallbackWrap(msg) =>
        msg match {
          case Scene.NextSceneCallback(genScene) =>
            presenterRef ! Presenter.Cleanup
            sceneRef ! Scene.ChangeScene(genScene())
          case Scene.Initialized(state, presenter) =>
            // TODO ここでsetupを呼ぶべきなのか？
            presenter.setup(state, cmd => sceneRef ! Scene.Execution(cmd))
            presenterRef ! Presenter.Initialize(presenter)
          case Scene.CurrentState(state) =>
            presenterRef ! Presenter.Update(state)
          case Scene.NoCallback =>
        }
        Behaviors.same
      case _ =>
        // TODO implement other cases.
        Behaviors.same
    }
  }
}
