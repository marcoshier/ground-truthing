import kotlinx.coroutines.runBlocking
import lib.Receiver
import org.openrndr.application
import org.openrndr.launch

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
                        e.paths.saveOBJasLines("data/obj/circle.obj")
                        println("plotting")
                    }
                }
            }
        }

        extend {


        }
    }
}