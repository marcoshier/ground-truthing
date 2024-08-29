import kotlinx.coroutines.runBlocking
import lib.Receiver
import org.openrndr.application
import org.openrndr.extra.shapes.path3d.toPath3D
import org.openrndr.launch
import org.openrndr.shape.ShapeContour
import kotlin.concurrent.thread

fun main() = application {

    program {


        val receiver = Receiver()


        receiver.stateReceived.listen { e ->
            runBlocking {
                when(e.i) {
                    0 -> {
                        println("idle")
                    }
                    1 -> {
                        println("tracking")
                    }
                    2 -> {

                        val paths = e.paths.map { ShapeContour.fromPoints(it.toList(), false).toPath3D() }
                        paths.saveOBJasLines("data/obj/circle.obj")
                        println("plotting")
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