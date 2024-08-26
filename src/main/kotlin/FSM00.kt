import classes.Orchestrator
import lib.Kinect
import org.openrndr.animatable.Animatable
import org.openrndr.application

fun main() = application {

    program {

        val kinect1 = Kinect()
        val kinect2 = Kinect()

        val orchestrator = Orchestrator()

        extend {

            val presence1 = kinect1.skeletons.isNotEmpty()
            val presence2 = kinect1.skeletons.isNotEmpty()

            orchestrator.update(presence1, presence2)

        }
    }
}
