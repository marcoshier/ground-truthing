import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import edu.ufl.digitalworlds.j4k.Skeleton
import kotlinx.coroutines.runBlocking
import lib.Kinect
import lib.Receiver
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.math.Vector2
import kotlin.concurrent.thread

fun main() = application {

    program {

        val kinect = Kinect()
        kinect.start(32)

        val receiver = Receiver()
        var tracking = false

        var positions = mutableListOf<Vector2>()
        var positions2 = listOf<Vector2>()

        receiver.stateReceived.listen { e ->
            runBlocking {
                when(e.i) {
                    0 -> {
                        positions.clear()
                    }
                    1 -> {
                        tracking = true
                    }
                    2 -> {
                        tracking = false
                        val points = e.points.mapValues { Vector2(it.value[0], it.value[1]) }.values.toList()
                        positions2 = points
                    }
                }
            }
        }

        keyboard.keyUp.listen {
            if (it.key == KEY_SPACEBAR) {

                val csv = csvWriter()

                csv.open("data/points1.csv") {
                    for (c in positions) {
                        writeRow(c.x, c.y)
                    }
                }

                csv.open("data/points2.csv") {
                    for (c in positions2) {
                        writeRow(c.x, c.y)
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
                    positions.add(it.get3DJoint(Skeleton.SPINE_MID).toVector2())
                }
            }

        }
    }
}