package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.akka.Presenter.PresenterCallback
import com.yuiwai.yachiyo.akka.View.ViewCallback
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui._

object Application {
  sealed trait ApplicationCommand
  final case class SceneCallbackWrap(sceneCallback: SceneCallback) extends ApplicationCommand
  final case class PresenterCallbackWrap(presenterCallback: PresenterCallback) extends ApplicationCommand
  final case class ViewCallbackWrap(viewCallback: ViewCallback) extends ApplicationCommand
  def behavior(application: ui.Application): Behavior[ApplicationCommand] = Behaviors.setup { ctx =>
    val sceneRef = ctx.spawn(Scene.init(ctx.self ! SceneCallbackWrap(_)), "scene")
    val presenterRef = ctx.spawn(Presenter.init(ctx.self ! PresenterCallbackWrap(_)), "presenter")
    val viewRef = ctx.spawn(View.init(ctx.self ! ViewCallbackWrap(_)), "view")

    def make(sceneSuite: SceneSuite): Behaviors.Receive[ApplicationCommand] = {
      sceneRef ! Scene.Initialize(sceneSuite.genScene)
      Behaviors.receiveMessage[ApplicationCommand] {
        case SceneCallbackWrap(msg) =>
          msg match {
            case NextSceneCallback(genScene) =>
              presenterRef ! Presenter.Cleanup
              sceneRef ! Scene.ChangeScene(genScene)
            case Initialized(state) =>
              presenterRef ! Presenter.Initialize(state, sceneSuite.genPresenter)
            case StateChangedCallback(state) =>
              presenterRef ! Presenter.Update(state)
            case NoCallback =>
          }
          Behaviors.same
        case PresenterCallbackWrap(msg) =>
          msg match {
            case Presenter.Initialized(viewModel) =>
              viewRef ! View.Initialize(sceneSuite.genView, viewModel)
              Behaviors.same
          }
        case ViewCallbackWrap(msg) =>
          msg match {
            case View.ExecutionCallback(command) =>
              sceneRef ! command
              Behaviors.same
          }
        case _ =>
          // TODO implement other cases.
          Behaviors.same
      }
    }

    def preparing(): Behaviors.Receive[ApplicationCommand] = {
      Behaviors.receiveMessage[ApplicationCommand] { _ => Behaviors.same }
    }

    make(application.initialSceneSuite)
  }
}

