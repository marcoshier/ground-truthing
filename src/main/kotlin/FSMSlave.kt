import edu.ufl.digitalworlds.j4k.Skeleton
import kotlinx.coroutines.runBlocking
import lib.Kinect
import lib.Receiver
import org.openrndr.application
import org.openrndr.extra.osc.OSC
import org.openrndr.extra.shapes.path3d.toPath3D
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.concurrent.thread

fun main() = application {

    program {

        val kinect = Kinect()
        kinect.start(32)

        val receiver = Receiver()
        val osc = OSC(portIn = 7001, portOut = 7000)

        var tracking = false

        var startTimeStamp = 0L
        val positions = mutableMapOf<Long, Vector2>()

        receiver.stateReceived.listen { e ->
            runBlocking {
                when(e.i) {
                    0 -> {
                        positions.clear()
                        osc.send("/td", 0)
                    }
                    1 -> {
                        tracking = true
                        println("tracking")
                    }
                    2 -> {
                        tracking = false
                        val points = e.points.mapValues { Vector2(it.value[0], it.value[1]) }
                        val allPoints = (points + positions).toSortedMap().values

                        val path = ShapeContour.fromPoints(allPoints.toList(), false).toPath3D()
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
            if (tracking) {
                kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)

                kinect.skeletons.filterNotNull().filter { it.isTracked }.forEach {
                    val t = System.currentTimeMillis() - startTimeStamp
                    positions[t] = it.get3DJoint(Skeleton.SPINE_MID).toVector2()
                }
            }

        }
    }
}