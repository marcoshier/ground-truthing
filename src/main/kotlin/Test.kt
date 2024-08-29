
import edu.ufl.digitalworlds.j4k.J4KSDK
import edu.ufl.digitalworlds.j4k.Skeleton
import edu.ufl.digitalworlds.j4k.VideoFrame
import lib.Kinect
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.events.Event
import org.openrndr.extra.color.spaces.ColorOKHSVa
import org.openrndr.extra.color.tools.shiftHue
import org.openrndr.extra.noclear.NoClear
import kotlin.math.sin

fun main() = application {
    configure {
        width = 1920
        height = 1080
    }
    program {


        val kinect = Kinect()
        kinect.start(32)

        val joints = listOf(Skeleton.HEAD, Skeleton.SPINE_BASE)

        extend(NoClear())
        extend {
            drawer.fill = ColorRGBa.BLACK.opacify(0.01)

            drawer.rectangle(drawer.bounds)

            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)



            val skeletons = kinect.skeletons.toList().filterNotNull()
            kinect.skeletonCountLimit
            kinect.maxNumberOfSkeletons
            skeletons.filter { it.isTracked }.forEachIndexed { i, it ->
                //   it.improve_skeleton()

                drawer.stroke = null
                for (j in joints) {
                    val xy = it.get2DJoint(j, width, height)

                    // drawer.stroke = ColorRGBa.GREEN
                    drawer.fill = ColorRGBa.RED.shiftHue<ColorOKHSVa>(60.0 * i + seconds * 20.0)

                    drawer.circle(xy[0].toDouble(), xy[1].toDouble(), 2.0 + (sin(seconds) * 15.0 + 15.0))


                }


            }

        }

    }
}
