package com.yuiwai.yachiyo.demo

import com.yuiwai.kasumi.core.implementation.{Node, TypedBoard}
import com.yuiwai.yachiyo.core.Pos
import com.yuiwai.yachiyo.demo.NodeDemoScene.BackToTop
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom.raw.{HTMLDivElement, HTMLElement}

object NodeDemoScene extends Scene {
  override type State = TypedBoard[Pos[Int]]
  override type Command = NodeDemoSceneCommand
  override type Event = None.type

  sealed trait NodeDemoSceneCommand
  case object BackToTop extends NodeDemoSceneCommand

  override def initialState(): State = TypedBoard.empty + (Pos(0, 0), Pos(1, 0))
  override def execute(state: State, input: NodeDemoSceneCommand): (State, None.type, SceneCallback) =
    input match {
      case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
    }
  override def cleanup(): Unit = {}
}

final class NodeDemoPresenter extends Presenter {
  override type S = NodeDemoScene.type
  override type M = NodeDemoViewModel
  override def updated(state: TypedBoard[Pos[Int]]): NodeDemoViewModel = NodeDemoViewModel(state)
}

final case class NodeDemoViewModel(board: TypedBoard[Pos[Int]]) extends ViewModel

final class NodeDemoView extends DomView with CommonView {
  override type M = NodeDemoViewModel
  override type S = NodeDemoScene.type
  private val stage: HTMLDivElement = div()
  override def setup(viewModel: NodeDemoViewModel, listener: Listener): Unit = {
    container.appendChild(button("Back To Top")
      .tap(_.onclick = { _ => listener(BackToTop) })
    )
    container.appendChild(stage)
  }
  override def update(viewModel: NodeDemoViewModel): Unit = {
    import viewModel.board

    stage.innerHTML = ""
    board.nodesV.foreach { n =>
      stage.appendChild(drawNodeAsHTMLElement(n))
    }
  }
  override def cleanup(): Unit = {
    stage.innerHTML = ""
    super.cleanup()
  }
  private def drawNodeAsHTMLElement(node: Node[Pos[Int]]): HTMLElement = {
    import node.value.{x, y}
    div().tap { e =>
      e.innerText = s"($x, $y)"
      e.style.display = "inline"
      e.style.position = "absolute"
      e.style.left = px(x * 60)
      e.style.cursor = "pointer"
      e.onmouseover = { _ => e.style.fontWeight = "bold" }
      e.onmouseout = { _ => e.style.fontWeight = "normal" }
    }
  }
}
