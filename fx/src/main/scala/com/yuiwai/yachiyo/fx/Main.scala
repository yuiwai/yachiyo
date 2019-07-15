package com.yuiwai.yachiyo.fx

import com.yuiwai.yachiyo.core._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

object Main extends JFXApp {
  private val canvas = new Canvas(500, 500)
  private val ctx = canvas.graphicsContext2D
  private var s = ParticleSystem[Double](
    Pos(250, 250),
    100,
    Seq.empty,
    Generator({ s =>
      s.spawn(Speed(0.0, 1.0) * Angle.random(0, 360))
    }, 1),
    Gravity(0.01, Angle.up) // Y座標が上下逆
  )
  reset()

  canvas.onMouseMoved = { e =>
    s = s.copy(pos = Pos(e.getX, e.getY)).updated()
    reset()

    ctx.fill = Color(1, 1, 1, 0.5)
    s.particles.foreach { p =>
      ctx.fillOval(p.pos.x, p.pos.y, 5, 5)
    }
  }

  stage = new PrimaryStage {
    title = "yachiyo-fx"
    scene = new Scene {
      content = canvas
    }
  }

  def reset(): Unit = {
    ctx.fill = Color.Black
    ctx.fillRect(0, 0, 500, 500)
  }
}
