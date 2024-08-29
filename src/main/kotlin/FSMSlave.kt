import kotlinx.coroutines.runBlocking
import lib.Receiver
import org.openrndr.application
import org.openrndr.extra.osc.OSC
import org.openrndr.extra.shapes.path3d.toPath3D
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.concurrent.thread

fun main() = application {

    program {


        val receiver = Receiver()
        val osc = OSC(portIn = 7001, portOut = 7000)


        receiver.stateReceived.listen { e ->
            runBlocking {
                when(e.i) {
                    0 -> {
                        osc.send("/td", 0)
                    }
                    1 -> {
                        println("tracking")
                    }
                    2 -> {
                        val points = e.points.toSortedMap().values.map { Vector2(it[0], it[1]) }
                        val path = ShapeContour.fromPoints(points, false).toPath3D()
                        listOf(path).saveOBJasLines("data/obj/circle.obj")

                        osc.send("/td", 1)
                    }
                }
            }
        }

        thread(isDaemon = true) {
            while (true) {
                receiver.work()
            }
        }

        extend {


        }
    }
}