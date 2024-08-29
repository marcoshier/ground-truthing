import org.openrndr.application
import org.openrndr.extra.shapes.path3d.toPath3D
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Path3D
import java.io.File

fun List<Path3D>.saveOBJasLines(filePath: String) {

    var result = ""

    for ((i, path) in withIndex()) {
        result += "o path_$i \n"
        val segments = path.sampleLinear().segments
        val points = segments.map { it.start }.distinct()

        for (p in points) {
            result += "v ${p.x} ${p.y} ${p.z} \n"
        }

        result += "\n"
        result += "l "
        for ((j, p) in (points).withIndex()) {
            result += "${j + 1} "
        }
    }

    val f = File(filePath)
    f.bufferedWriter().use { writer ->
        writer.run {
            // Write header
            append(result)
        }
    }

    println(result)

}

fun main() = application {

    program {

        val path = Circle(Vector2.ZERO, 100.0).contour.toPath3D()
        val obj = listOf(path).saveOBJasLines("data/obj/circle.obj")


        extend {

        }
    }

}
