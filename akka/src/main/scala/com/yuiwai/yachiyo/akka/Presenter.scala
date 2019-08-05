package com.yuiwai.yachiyo.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.ViewModel

object Presenter {
  type GenPresenter[M <: ViewModel] = () => ui.Presenter
  sealed trait PresenterCommand
  final case class Initialize[S <: ui.Scene, M <: ViewModel](initialState: S#State, genPresenter: GenPresenter[M]) extends PresenterCommand
  final case class Update[S <: ui.Scene](state: S#State) extends PresenterCommand
  case object Cleanup extends PresenterCommand

  sealed trait PresenterCallback
  final case class Initialized(viewModel: ViewModel) extends PresenterCallback
  case object CleanedUp extends PresenterCallback
  final case class Updated[M <: ViewModel](viewModel: ViewModel) extends PresenterCallback

  def deployed[S <: ui.Scene, M <: ViewModel]
  (presenter: ui.Presenter, listener: PresenterCallback => Unit, initialModel: Option[M]): Behaviors.Receive[PresenterCommand] = {
    make(
      presenter,
      listener,
      if (presenter.usePrevModel) initialModel.map(_.asInstanceOf[presenter.M]) else None
    )
  }
  private def make[M](presenter: ui.Presenter, listener: PresenterCallback => Unit, prevModel: Option[M]): Behaviors.Receive[PresenterCommand] = {
    Behaviors.receive[PresenterCommand] { (_, msg) =>
      msg match {
        case Cleanup =>
          presenter.cleanup()
          listener(CleanedUp)
          init(listener)
        case Update(state) =>
          val viewModel = presenter.updated(state.asInstanceOf[presenter.S#State], prevModel.map(_.asInstanceOf[presenter.M]))
          listener(Updated(viewModel))
          make(presenter, listener, if (presenter.usePrevModel) Some(viewModel) else None)
        case _ => Behaviors.same
      }
    }
  }
  def init[S <: ui.Scene](listener: PresenterCallback => Unit): Behavior[PresenterCommand] =
    Behaviors.receive { (_, msg) =>
      msg match {
        case Initialize(initialState, genPresenter) =>
          val presenter = genPresenter()
          val viewModel = presenter.setup(initialState.asInstanceOf[presenter.S#State])
          listener(Initialized(viewModel))
          deployed(presenter, listener, if (presenter.usePrevModel) Some(viewModel) else None)
        case _ => Behaviors.same
      }
    }
}

