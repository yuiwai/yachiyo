package com.yuiwai.yachiyo.demo.zio

import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.{NextSceneCallback, NoCallback, SceneCallback, SceneSuite}
import com.yuiwai.yachiyo.zio.ApplicationHandler
import com.yuiwai.yachiyo.zio.ApplicationHandler.AppEnv
import zio.DefaultRuntime

import scala.concurrent.Future

object Demo extends DefaultRuntime {
  def main(args: Array[String]): Unit = {
    unsafeRun(ApplicationHandler.program.provide(AppEnv.unsafeInit(DemoApp)))
  }
}

object DemoApp extends ui.Application {
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    1 -> SceneSuite(
      () => DemoScene,
      () => DemoPresenter,
      () => DemoView
    ),
    2 -> SceneSuite(
      () => DemoScene2,
      () => DemoPresenter2,
      () => DemoView2
    )
  )
  override def initialSceneSuiteKey: Int = 1
}

object DemoScene extends ui.Scene {
  override type State = Int
  override type Command = DemoSceneCmd
  override type Event = None.type

  sealed trait DemoSceneCmd
  case object SayHello extends DemoSceneCmd
  case object Next extends DemoSceneCmd

  override def initialState(): Int = 0
  override def execute(state: Int, input: DemoSceneCmd): (Int, None.type, SceneCallback) = input match {
    case SayHello => (state + 1, None, NoCallback)
    case Next => (state, None, NextSceneCallback(2))
    case _ =>
      println(input)
      (state, None, NoCallback)
  }
}
object DemoScene2 extends ui.Scene {
  override type State = String
  override type Command = DemoSceneCmd
  override type Event = None.type

  sealed trait DemoSceneCmd
  case object Goodbye extends DemoSceneCmd

  override def initialState(): String = ""
  override def execute(state: String, input: DemoSceneCmd): (String, None.type, SceneCallback) = input match {
    case _ =>
      println(input)
      (state, None, NoCallback)
  }
}

object DemoPresenter extends ui.Presenter {
  override type S = DemoScene.type
  override type M = DemoViewModel
  override def setup(initialState: Int): DemoViewModel = {
    super.setup(initialState)
  }
  override def updated(state: DemoScene.State, prevModel: DemoPresenter.Prev): DemoViewModel = DemoViewModel(state)
}
object DemoPresenter2 extends ui.Presenter {
  override type S = DemoScene2.type
  override type M = DemoViewModel
  override def setup(initialState: String): DemoViewModel = {
    super.setup(initialState)
  }
  override def updated(state: DemoScene2.State, prevModel: DemoPresenter.Prev): DemoViewModel = DemoViewModel(state.length)
}

final case class DemoViewModel(value: Int) extends ui.ViewModel
object DemoView extends ui.View {
  import scala.concurrent.ExecutionContext.Implicits.global
  override type S = DemoScene.type
  override type M = DemoViewModel
  override def setup(viewModel: DemoViewModel, listener: DemoView.Listener): Unit = {
    update(viewModel)
    Future {
      Thread.sleep(1000)
      listener(DemoScene.SayHello)
    }
    Future {
      Thread.sleep(2000)
      listener(DemoScene.Next)
    }
  }
  override def update(viewModel: DemoViewModel): Unit = {
    println(s"view: $viewModel")
  }
  override def cleanup(): Unit = ()
}
object DemoView2 extends ui.View {
  import scala.concurrent.ExecutionContext.Implicits.global
  override type S = DemoScene2.type
  override type M = DemoViewModel
  override def setup(viewModel: DemoViewModel, listener: DemoView2.Listener): Unit = {
    update(viewModel)

    Future {
      listener(DemoScene2.Goodbye)
    }
  }
  override def update(viewModel: DemoViewModel): Unit = {
    println(s"view: $viewModel")
  }
  override def cleanup(): Unit = ()
}

