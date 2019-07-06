package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

trait Presenter {
  type S <: Scene
  type M <: ViewModel
  def setup(initialState: S#State): M = updated(initialState)
  def cleanup(): Unit = {}
  def updated(state: S#State): M
}
object Presenter {
  type GenPresenter[M <: ViewModel] = () => Presenter
  sealed trait PresenterCommand
  final case class Initialize[S <: Scene, M <: ViewModel](initialState: S#State, genPresenter: GenPresenter[M]) extends PresenterCommand
  final case class Update[S <: Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ViewModel) extends PresenterCallback
  final case class Updated[M <: ViewModel](viewModel: ViewModel) extends PresenterCallback

  def deployed[S <: Scene, M <: ViewModel](presenter: Presenter, listener: PresenterCallback => Unit): Behaviors.Receive[PresenterCommand] = {
    def make(): Behaviors.Receive[PresenterCommand] = {
      Behaviors.receive[PresenterCommand] { (_, msg) =>
        msg match {
          case Cleanup =>
            presenter.cleanup()
            Behaviors.same
          case Update(state) =>
            val viewModel = presenter.updated(state.asInstanceOf[presenter.S#State])
            // TODO ここでViewを更新したい
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
        case Initialize(initialState, genPresenter) =>
          val presenter = genPresenter()
          listener(Initialized(presenter.setup(initialState.asInstanceOf[presenter.S#State])))
          deployed(presenter, listener)
        case _ => Behaviors.same
      }
    }
}

