import classes.Orchestrator
import edu.ufl.digitalworlds.j4k.Skeleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lib.Kinect
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.shapes.path3d.toPath3D
import org.openrndr.math.Vector2
import org.openrndr.shape.Path3D
import org.openrndr.shape.ShapeContour
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.thread

data class Message(val i: Int, val paths: List<List<Vector2>>): Serializable

fun main() = application {

    program {

        val test = true

        val orchestrator = Orchestrator()

        val sendChannel = Channel<Message>(10000)
        val ipAddress = "169.254.130.90"

        val testPaths = listOf((0..10).map { drawer.bounds.uniform() })

        val positions = mutableListOf<Vector2>()

        thread(isDaemon = true) {
            val socket = DatagramSocket()
            val address = InetSocketAddress(InetAddress.getByName(ipAddress), 9002)
            socket.soTimeout = 10
            val baos = ByteArrayOutputStream(16384)
            fun send(state: Message) {
                baos.reset()

                ObjectOutputStream(baos).use {
                    it.writeUnshared(state)
                    val dt = baos.toByteArray()
                    val p = DatagramPacket(dt, dt.size, address)
                    socket.send(p)
                }
            }

            while (true) {
                runBlocking {
                    val eo = sendChannel.receive()
                    send(eo)
                }
            }
        }



        orchestrator.idleEvent.listen {
            positions.clear()
            runBlocking {
                sendChannel.send(Message(0, listOf()))
            }
        }

        orchestrator.trackEvent.listen {
            runBlocking {
                sendChannel.send(Message(1, listOf()))
            }
        }

        orchestrator.plotEvent.listen {
            if (!test) {
                val groups = mutableListOf(mutableListOf<Vector2>())
                val currentPointSet = mutableListOf<Vector2>()

                for ((i, p) in positions.withIndex()) {
                    if (i != 0 && p.distanceTo(positions[i - 1]) < 5.0) {
                        currentPointSet.add(p)
                    } else {
                        groups.add(currentPointSet)
                        currentPointSet.clear()
                    }
                }

                runBlocking {
                    sendChannel.send(Message(2, groups.map { it.toList() }))
                }
            } else {
                runBlocking {
                    sendChannel.send(Message(2, testPaths))
                }
            }
            println("sending")
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) {
                orchestrator.plotEvent.trigger(Unit)
            }
        }

        val kinect = Kinect()
        kinect.start(32)


        extend {
            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)

            kinect.skeletons.filterNotNull().filter { it.isTracked }.forEach {
                positions.add(it.get3DJoint(Skeleton.SPINE_MID).toVector2())
            }

        }
    }
}