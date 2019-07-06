package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Presenter.PresenterCallback
import com.yuiwai.yachiyo.akka.Scene.SceneCallback

trait Application {
  protected def initialSceneSuite: SceneSuite
}
object Application {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  def behavior(application: Application): Behavior[ApplicationCommand] = Behaviors.setup { ctx =>
    val sceneRef = ctx.spawn(Scene.init(ctx.self ! SceneCallbackWrap(_)), "scene")
    val presenterRef = ctx.spawn(Presenter.init(ctx.self ! PresenterCallbackWrap(_)), "presenter")
    val viewRef = ctx.spawn(View.init(), "view")

    def make(sceneSuite: SceneSuite): Behaviors.Receive[ApplicationCommand] = {
      sceneRef ! Scene.Initialize(sceneSuite.genScene)
      Behaviors.receiveMessage[ApplicationCommand] {
        case SceneCallbackWrap(msg) =>
          msg match {
            case Scene.NextSceneCallback(genScene) =>
              presenterRef ! Presenter.Cleanup
              sceneRef ! Scene.ChangeScene(genScene())
            case Scene.Initialized(state) =>
              presenterRef ! Presenter.Initialize(state, sceneSuite.genPresenter)
            case Scene.StateChangedCallback(state) =>
              presenterRef ! Presenter.Update(state)
            case Scene.NoCallback =>
          }
          Behaviors.same
        case PresenterCallbackWrap(msg) =>
          msg match {
            case Presenter.Initialized(viewModel) =>
              viewRef ! View.Initialize(sceneSuite.genView, viewModel, sceneRef)
              Behaviors.same
          }
        case _ =>
          // TODO implement other cases.
          Behaviors.same
      }
    }

    make(application.initialSceneSuite)
  }
}

abstract class SceneSuite {
  val genScene: () => Scene
  val genPresenter: () => Presenter
  val genView: () => View
}

