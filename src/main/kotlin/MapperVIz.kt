import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import java.io.File

fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    oliveProgram {

        val rect0 = Rectangle(0.0, 0.0, 150.0, 150.0)
        val rect1 = Rectangle(200.0, 0.0, 150.0, 150.0)

        val points1 = mutableListOf<Vector2>()
        csvReader().readAll(File("data/points1.csv")).forEach {
            points1.add(Vector2(it[0].toDouble(), it[1].toDouble()))
        }

        val points2 = mutableListOf<Vector2>()
        csvReader().readAll(File("data/points2.csv")).forEach {
            points2.add(Vector2(it[0].toDouble(), it[1].toDouble()))
        }

        val mapped = false

        extend {

            if (mapped) {
                drawer.fill = ColorRGBa.BLUE
                drawer.circles(points1.map(points1.bounds, rect0), 4.0)

                drawer.fill = ColorRGBa.RED
                drawer.circles(points2.map(points2.bounds, rect1), 4.0)
            } else {
                drawer.fill = ColorRGBa.BLUE
                drawer.circles(points1, 4.0)

                drawer.fill = ColorRGBa.RED
                drawer.circles(points2, 4.0)
            }

        }
    }
}