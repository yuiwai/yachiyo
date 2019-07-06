package com.yuiwai.yachiyo.demo

import com.yuiwai.yachiyo.akka.Scene.{NextSceneCallback, NoCallback, SceneCallback}
import com.yuiwai.yachiyo.akka.{DomView, Presenter, Scene, ViewModel}
import com.yuiwai.yachiyo.core._
import com.yuiwai.yachiyo.demo.TransitionDemoScene.{BackToTop, TogglePlaying}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement

object TransitionDemoScene extends Scene {
  override type State = Boolean
  override type Command = TransitionDemoMsg
  override type Event = None.type
  sealed trait TransitionDemoMsg
  case object TogglePlaying extends TransitionDemoMsg
  case object BackToTop extends TransitionDemoMsg

  override def initialState(): Boolean = false
  override def execute(state: Boolean, input: TransitionDemoMsg): (Boolean, Event, SceneCallback) = input match {
    case TogglePlaying => (!state, None, NoCallback)
    case BackToTop => (state, None, NextSceneCallback(() => TopDemoScene))
  }
  override def cleanup(): Unit = {}
}

// FIXME 中身をView/ViewModelに切り分ける
class TransitionDemoPresenter extends Presenter {
  override type M = TransitionViewModel
  override type S = TransitionDemoScene.type
  override def updated(state: Boolean): TransitionViewModel = TransitionViewModel(state)
}

final case class TransitionViewModel(playing: Boolean) extends ViewModel

class TransitionView extends DomView {
  override type S = TransitionDemoScene.type
  override type M = TransitionViewModel
  private val duration = 2000
  private var divs = Seq.empty[HTMLDivElement]
  private def container = elementById("container")
  private var playing = false
  private val t1 = Transition(0, 500, Progress(0, duration, 0))
  private val transitions = Seq(
    t1,
    t1.withExtension(SinEaseInExtension),
    t1.withExtension(SinEaseOutExtension),
    t1.withExtension(CycleRateExtension),
    t1.withExtension(CompositeExtension(SinEaseInExtension, ReverseRateExtension)),
    t1.withExtension(CompositeExtension(SinEaseInExtension, CycleRateExtension)),
    t1.withExtension(CompositeExtension(CycleRateExtension, SinEaseInExtension)),
    t1.withExtension(CompositeExtension(SinEaseOutExtension, CycleRateExtension)),
    t1.withExtension(CompositeExtension(CycleRateExtension, SinEaseOutExtension))
  )
  override def setup(viewModel: TransitionViewModel, listener: Listener): Unit = {
    val btn = button("Back To Top")
    btn.onclick = _ => listener(BackToTop)
    container.appendChild(btn)
    (1 to 9).foreach { i =>
      val div = createElementAs[HTMLDivElement]("div")
      div.style =
        s"""
           |position: absolute;
           |width: 10px;
           |height: 10px;
           |top: ${i * 20 + 30}px;
           |background-color: green;
        """.stripMargin
      container.appendChild(div)
      divs = divs :+ div
    }
    dom.window.onclick = _ => listener(TogglePlaying)
  }
  override def cleanup(): Unit = {
    container.innerHTML = ""
    dom.window.onclick = null
  }
  override def draw(viewModel: TransitionViewModel): Unit = {
    if (viewModel.playing) start()
    else stop()
  }
  private def start(): Unit = {
    playing = true
    dom.window.requestAnimationFrame(animation)
  }
  private def stop(): Unit = {
    playing = false
  }
  private def animation(t: Double): Unit = {
    val p = (t % duration).toInt
    divs.zip(transitions).foreach { case (div, transition) =>
      div.style.left = transition.past(p).value + "px"
    }
    if (playing) dom.window.requestAnimationFrame(animation)
  }
}
