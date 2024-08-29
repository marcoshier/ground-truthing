
import lib.loadOBJasLinesVertexBuffer
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.extra.camera.Orbital
import org.openrndr.extra.camera.OrbitalCamera
import org.openrndr.extra.camera.OrbitalControls
import org.openrndr.extra.objloader.loadOBJ
import org.openrndr.extra.objloader.loadOBJasVertexBuffer
import org.openrndr.math.Vector3

fun main() = application {


    program {

        val load = loadOBJasLinesVertexBuffer("data/obj/circle.obj")

        val camera = OrbitalCamera(Vector3.ONE * 5.0, Vector3.ZERO, 90.0, 0.1, 5000.0)
        val controls = OrbitalControls(camera)

        //println(load.vertexCount)

        extend(camera)
        extend(controls)
        extend {

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = ColorRGBa.WHITE
            drawer.vertexBuffer(load, DrawPrimitive.LINES)
        }
    }
}