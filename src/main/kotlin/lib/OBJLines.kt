package lib


import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.math.Vector3
import org.openrndr.shape.Path3D
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun loadOBJasLinesVertexBuffer(path: String): VertexBuffer {
    val vfPos = vertexFormat { position(3) }
    val lines = loadOBJasLines(path)
    println(lines.size)
    val vb = vertexBuffer(vfPos, lines.sumOf { it.segments.size } * 2)
    vb.put {
        lines.forEach {
            write(it.position(0.0))
            write(it.position(1.0))
        }
    }
    return vb
}

fun loadOBJasLines(path: String): List<Path3D> {
    val f = File(path)
    val lines = f.readLines()
    val result = mutableListOf<Path3D>()
    val positions = mutableListOf<Vector3>()
    lines.forEach { line ->
        if (line.isNotEmpty()) {
            val tokens = line.split(Regex("[ |\t]+")).map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isNotEmpty()) {
                when (tokens[0]) {
                    "v" -> positions += Vector3(tokens[1].toDouble(), tokens[2].toDouble(), tokens[3].toDouble())

                    "l" -> {
                        val points = tokens.drop(1).map {
                            positions[it.toInt() - 1]
                        }
                        println(positions.size)
                        result.add(Path3D.fromPoints(points, false))
                    }
                }
            }
        }
    }
    return result
}