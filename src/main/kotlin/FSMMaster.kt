import classes.Orchestrator
import edu.ufl.digitalworlds.j4k.Skeleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lib.Kinect
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.concurrent.thread


data class Message(val i: Int, val points: Map<Long, List<Double>>): Serializable

fun main() = application {

    program {

        val test = true

        val orchestrator = Orchestrator()

        val sendChannel = Channel<Message>(10000)
        val ipAddress = "169.254.130.90"

        val testPaths = (0..10).map { drawer.bounds.uniform() }.associate { System.currentTimeMillis() to listOf(it.x, it.y) }

        var startTimeStamp = 0L
        val positions = mutableMapOf<Long, Vector2>()

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
                sendChannel.send(Message(0, mapOf()))
            }
        }

        orchestrator.trackEvent.listen {
            startTimeStamp = System.currentTimeMillis()
        }

        orchestrator.plotEvent.listen {
            if (!test) {
                val currentPointSet = mutableMapOf<Long, Vector2>()

                var i = 0
                var last = 0L
                for ((t, p) in positions) {
                    currentPointSet[t] = p
                }

                runBlocking {
                    //it.toList().associate { it.first to listOf(it.second.x, it.second.y)  })
                    sendChannel.send(Message(2, currentPointSet.mapValues { listOf(it.value.x, it.value.y) }))
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
                val t = System.currentTimeMillis() - startTimeStamp
                positions[t] = it.get3DJoint(Skeleton.SPINE_MID).toVector2()
            }

        }
    }
}