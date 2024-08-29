@file:Suppress("UNREACHABLE_CODE")

import classes.Orchestrator
import edu.ufl.digitalworlds.j4k.Skeleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lib.Kinect
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.extra.noise.uniform
import org.openrndr.launch
import org.openrndr.math.Vector2
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.concurrent.thread


fun main() = application {

    program {
        val sendChannel = Channel<Message>(10000)
        val ipAddress = "192.168.1.5"

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


        launch {
            while (true) {
                sendChannel.send(Message(2, positions.mapValues { listOf(it.value.x, it.value.y) }))
            }
        }

        val kinect = Kinect()
        kinect.start(32)


        extend {
            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)

            kinect.skeletons.filterNotNull().filter { it.isTracked }.forEach {
                positions[System.currentTimeMillis()] = it.get3DJoint(Skeleton.SPINE_MID).toVector2()
            }

        }
    }
}