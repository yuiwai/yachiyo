package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

trait Presenter[S <: Scene] {
  type Listener = S#Command => Unit
  def setup(initialState: S#State, listener: Listener): Unit
  def cleanup(): Unit
  def updated(state: S#State): Unit
}
object Presenter {
  sealed trait PresenterCommand
  final case class Initialize[S <: Scene](presenter: Presenter[S]) extends PresenterCommand
  final case class Update[S <: Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  case object Initialized extends PresenterCallback

  def deployed[S <: Scene](presenter: Presenter[S], listener: PresenterCallback => Unit): Behaviors.Receive[PresenterCommand] = {
    def make(): Behaviors.Receive[PresenterCommand] = {
      Behaviors.receive[PresenterCommand] { (_, msg) =>
        msg match {
          case Initialize(newPresenter) =>
            deployed(newPresenter, listener)
          case Cleanup =>
            presenter.cleanup()
            Behaviors.same
          case Update(state) =>
            presenter.updated(state.asInstanceOf[S#State])
            Behaviors.same
          case _ =>
            // TODO other behaviors
            Behaviors.same
        }
      }
    }
    make()
  }
  def init[S <: Scene](listener: PresenterCallback => Unit): Behavior[PresenterCommand] =
    Behaviors.receive { (_, msg) =>
      msg match {
        case Initialize(presenter) => deployed(presenter, listener)
        case _ => Behaviors.same
      }
    }
}

