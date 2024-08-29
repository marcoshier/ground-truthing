import classes.Orchestrator
import classes.Stage
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.osc.OSC
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

fun main() = application {

    program {

        val osc = OSC(portIn = 7000, portOut = 7001)

        val positions = mutableListOf<Vector2>()
        val orchestrator = Orchestrator()

        var showContours = listOf<ShapeContour>()

        orchestrator.idleEvent.listen {
            osc.send("/td/state/", 0)
        }

        orchestrator.trackEvent.listen {
            osc.send("/td/state/", 1)
        }

        orchestrator.plotEvent.listen {
            osc.send("/td/state/", 2)
            val contours = mutableListOf<ShapeContour>()
            val currentPointSet = mutableListOf<Vector2>()

            for ((i, p) in positions.withIndex()) {
                currentPointSet.add(p)
                if (i != 0) {
                    if (p.distanceTo(currentPointSet.last()) > 50.0 || p == positions.last()) {
                        contours.add(ShapeContour.fromPoints(currentPointSet, false))
                        currentPointSet.clear()
                    } else {
                        currentPointSet.add(p)
                    }
                }
            }


            // send osc
            val cleanPoints = contours.map { it.equidistantPositions(100) }
            println(cleanPoints)
            val message = run {
                var str = ""

                for (c in cleanPoints.filter { it.isNotEmpty() }) {
                    str += "/"
                    for (p in c) {
                        str += "-${p.x.toString().take(4)},${p.y.toString().take(4)}"
                    }
                }

                str
            }

            println(message)
            osc.send("/td/points/", message)
            showContours = contours

            positions.clear()
        }

        extend {

            val presence1 = mouse.position in drawer.bounds.sub(0.0, 0.0, 0.5, 1.0)
            val presence2 = mouse.position in drawer.bounds.sub(0.5, 0.0, 1.0, 1.0)

            orchestrator.update(presence1, presence2)

            if (orchestrator.currentStage == Stage.TRACKING) {
                positions.add(mouse.position)
            }

            for (c in showContours) {
                drawer.stroke = ColorRGBa.WHITE
                drawer.contour(c)
            }

            drawer.fill = ColorRGBa.WHITE
            drawer.text(orchestrator.currentStage.toString(), 20.0, 20.0)
            drawer.text(orchestrator.presence.toString(), 20.0, 40.0)
            drawer.text("time since tracked ${orchestrator.timeSinceTracked}", 20.0, 60.0)
            drawer.text("time since calling ${orchestrator.timeSinceCalling}", 20.0, 80.0)
            drawer.text("time since plotting ${orchestrator.timeSincePlotting}", 20.0, 100.0)
        }
    }
}
