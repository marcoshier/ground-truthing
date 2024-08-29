package lib

import Message
import mu.KotlinLogging
import org.openrndr.events.Event
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.net.*

private val logger = KotlinLogging.logger {  }

class Receiver {
    val stateReceived = Event<Message>()
    val quit = false

    private val networkInterface = NetworkInterface.getNetworkInterfaces().toList().find { ni ->
        !ni.isVirtual && ni.isUp && !ni.isVirtual && ni.supportsMulticast() && ni.interfaceAddresses.find { ia ->
            val ipStart = System.getenv("screenIP") ?: System.getProperty("screenIP") ?: "172."
            ia.address.hostAddress.startsWith(ipStart)
        }!= null }.also { println(it) }

    fun work() {
        val serverSocket: DatagramSocket
        try {
            val addr = networkInterface!!.inetAddresses.nextElement()
            serverSocket = DatagramSocket(InetSocketAddress(addr, 9002)).also { println(addr) }
        } catch (e1: SocketException) {
            e1.printStackTrace()
            return
        }

        val receiveData = ByteArray(65536)

        logger.info { "starting to receive packets on ${networkInterface.interfaceAddresses}" }
        while (!quit) {
            val receivePacket = DatagramPacket(receiveData, receiveData.size)
            try {
                serverSocket.receive(receivePacket)
                val `is` = ByteArrayInputStream(receiveData)
                val ois = ObjectInputStream(`is`)
                val `object` = ois.readObject()

                if (`object` is Message) {
                    stateReceived.trigger((`object`))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        logger.info { "stopped receiving packets" }
    }
}