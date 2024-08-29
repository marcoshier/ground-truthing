import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import edu.ufl.digitalworlds.j4k.Skeleton
import lib.Kinect
import lib.convexHull
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {



    program {

        val k1 = Kinect()
        k1.start(32)
        val k2 = Kinect()
        k2.start(32)


        var points1 = mutableListOf<Vector2>()
        var points2 = mutableListOf<Vector2>()

        keyboard.keyUp.listen {
            val ch1 = convexHull(points1)
            val ch2 = convexHull(points2)

            val csv = csvWriter()

            csv.open("data/ch1.csv") {
                for (c in ch1) {
                    writeRow(c.x, c.y)
                }
            }
            csv.open("data/ch2.csv") {
                for (c in ch2) {
                    writeRow(c.x, c.y)
                }
            }

        }

        extend {

            k1.videoTexture.update(k1.colorWidth, k1.colorHeight, k1.colorFrame)
            k2.videoTexture.update(k2.colorWidth, k2.colorHeight, k2.colorFrame)

            k1.skeletons.filterNotNull().filter { it.isTracked }.forEach {
                points1.add(it.get3DJoint(Skeleton.HEAD).toVector2())
            }
            k2.skeletons.filterNotNull().filter { it.isTracked }.forEach {
                points2.add(it.get3DJoint(Skeleton.HEAD).toVector2())
            }

            drawer.stroke = null
            drawer.fill = ColorRGBa.RED
            drawer.circles(points1.map(points1.bounds, drawer.bounds), 3.0)

            drawer.fill = ColorRGBa.BLUE
            drawer.circles(points2.map(points1.bounds, drawer.bounds), 3.0)



        }
    }
}

fun DoubleArray.toVector2(): Vector2 {
    return Vector2(this[0], this[2])
}