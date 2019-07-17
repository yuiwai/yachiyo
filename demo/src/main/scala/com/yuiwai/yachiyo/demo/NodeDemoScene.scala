package com.yuiwai.yachiyo.demo

import com.yuiwai.kasumi.core.implementation.{Node, TypedBoard, TypedEdge}
import com.yuiwai.yachiyo.core.Pos
import com.yuiwai.yachiyo.demo.NodeDemoScene.NodeDemoState
import com.yuiwai.yachiyo.ui._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{SVGElement, SVGRectElement}

object NodeDemoScene extends Scene {
  override type State = NodeDemoState
  override type Command = NodeDemoSceneCommand
  override type Event = None.type

  final case class NodeDemoState(board: TypedBoard[Pos[Int]], currentPos: Pos[Int]) {
    def moveRight(): NodeDemoState = updateIfCurrentPosExists(currentPos.modX(_ + 1))
    def moveLeft(): NodeDemoState = updateIfCurrentPosExists(currentPos.modX(_ - 1))
    def moveUp(): NodeDemoState = updateIfCurrentPosExists(currentPos.modY(_ - 1))
    def moveDown(): NodeDemoState = updateIfCurrentPosExists(currentPos.modY(_ + 1))
    private def updateIfCurrentPosExists(pos: Pos[Int]): NodeDemoState = pos match {
      case c if board.nodesV.contains(Node(c)) => copy(currentPos = c)
      case _ => this
    }
  }

  sealed trait NodeDemoSceneCommand
  case object BackToTop extends NodeDemoSceneCommand
  case object MoveLeft extends NodeDemoSceneCommand
  case object MoveRight extends NodeDemoSceneCommand
  case object MoveUp extends NodeDemoSceneCommand
  case object MoveDown extends NodeDemoSceneCommand

  override def initialState(): State = NodeDemoState(
    TypedBoard.empty +
      (Pos(0, 0), Pos(1, 0)) +
      (Pos(0, 0), Pos(1, 1)) +
      (Pos(1, 0), Pos(2, 0)) +
      (Pos(1, 1), Pos(2, 0)) +
      (Pos(1, 1), Pos(2, 1)) +
      (Pos(2, 0), Pos(3, 0)) +
      (Pos(2, 0), Pos(3, 1)) +
      (Pos(2, 1), Pos(3, 1)) +
      (Pos(2, 1), Pos(3, 2)) +
      (Pos(3, 1), Pos(4, 1)),
    Pos(2, 1)
  )

  override def execute(state: State, input: NodeDemoSceneCommand): (State, None.type, SceneCallback) =
    input match {
      case BackToTop => (state, None, NextSceneCallback(DemoApplication.TopSceneKey))
      case MoveLeft => (state.moveLeft(), None, NoCallback)
      case MoveRight => (state.moveRight(), None, NoCallback)
      case MoveUp => (state.moveUp(), None, NoCallback)
      case MoveDown => (state.moveDown(), None, NoCallback)
    }
  override def cleanup(): Unit = {}
}

final class NodeDemoPresenter extends Presenter {
  override type S = NodeDemoScene.type
  override type M = NodeDemoViewModel
  override def updated(state: NodeDemoState, prevModel: Prev): NodeDemoViewModel =
    NodeDemoViewModel(state.board, state.currentPos)
}

final case class NodeDemoViewModel(board: TypedBoard[Pos[Int]], currentPos: Pos[Int]) extends ViewModel

final class NodeDemoView extends SvgView with CommonView {
  override type M = NodeDemoViewModel
  override type S = NodeDemoScene.type
  private val stage: SVGElement = svg()
  private val nodeWidth = 80
  private val nodeHeight = 35
  private val nodeMargin = 15
  private val fontSize = 18
  private val cornerRound = 10
  private def center() = Pos(stage.clientWidth / 2, stage.clientHeight / 2)
  private def calcOffset(currentPos: Pos[Int]) = {
    val c = center()
    val current = Pos(currentPos.x * (nodeWidth + nodeMargin), currentPos.y * (nodeHeight + nodeMargin))
    Pos(c.x - nodeWidth / 2 - current.x, c.y - nodeHeight / 2 - current.y)
  }
  override def setup(viewModel: NodeDemoViewModel, listener: Listener): Unit = {
    container.appendChild(div(button("Back To Top")
      .tap(_.onclick = { _ => listener(NodeDemoScene.BackToTop) })
    ))
    container.appendChild(stage)
    dom.window.onkeydown = { e =>
      e.keyCode match {
        case KeyCode.L => listener(NodeDemoScene.MoveRight)
        case KeyCode.H => listener(NodeDemoScene.MoveLeft)
        case KeyCode.K => listener(NodeDemoScene.MoveUp)
        case KeyCode.J => listener(NodeDemoScene.MoveDown)
        case _ =>
      }
    }
  }
  override def update(viewModel: NodeDemoViewModel): Unit = {
    import viewModel.board

    stage.innerHTML = ""
    board.nodesV.foreach { n =>
      stage.appendChild(drawNodeAsSVGElement(n, viewModel.currentPos))
    }
    board.edges.foreach { e =>
      stage.appendChild(drawEdgeAsSVGElement(e, viewModel.currentPos))
    }
  }
  override def cleanup(): Unit = {
    stage.innerHTML = ""
    dom.window.onkeydown = null
    super.cleanup()
  }
  private def drawNodeAsSVGElement(node: Node[Pos[Int]], currentPos: Pos[Int]): SVGElement = {
    import node.value.{x, y}
    val offset = calcOffset(currentPos)
    group(
      createElementNS("rect").asInstanceOf[SVGRectElement]
        .tap { rect =>
          rect.setAttribute("x", px(x * (nodeWidth + nodeMargin) + offset.x))
          rect.setAttribute("y", px(y * (nodeHeight + nodeMargin) + offset.y))
          rect.setAttribute("rx", px(cornerRound))
          rect.setAttribute("fill", if (node.value == currentPos) "red" else "pink")
          rect.setAttribute("width", px(nodeWidth))
          rect.setAttribute("height", px(nodeHeight))
        },
      text(s"($x, $y)")
        .tap { txt =>
          txt.setAttribute("font-size", px(fontSize))
          txt.setAttribute("text-anchor", "middle")
          txt.setAttribute("baseline", "middle")
          txt.setAttribute("x", px(x * (nodeWidth + nodeMargin) + nodeWidth / 2 + offset.x))
          txt.setAttribute("y", px(y * (nodeHeight + nodeMargin) + ((nodeHeight + fontSize) / 2.1).toInt + offset.y))
          txt.setAttribute("width", px(nodeWidth))
          txt.setAttribute("height", px(nodeHeight))
        }
    )
  }
  private def drawEdgeAsSVGElement(edge: TypedEdge[Pos[Int]], currentPos: Pos[Int]): SVGElement = {
    import edge.from.value.{x => fx, y => fy}
    import edge.to.value.{x => tx, y => ty}
    val offset = calcOffset(currentPos)
    group(
      line()
        .tap { l =>
          l.setAttribute("x1", px((fx + 1) * nodeWidth + fx * nodeMargin + offset.x))
          l.setAttribute("x2", px(tx * (nodeWidth + nodeMargin) + offset.x))
          l.setAttribute("y1", px(fy * (nodeHeight + nodeMargin) + nodeHeight / 2 + offset.y))
          l.setAttribute("y2", px(ty * (nodeHeight + nodeMargin) + nodeHeight / 2 + offset.y))
          l.setAttribute("stroke-width", "1")
        }
    )
      .tap { g =>
        g.setAttribute("stroke", "black")
      }
  }
}
