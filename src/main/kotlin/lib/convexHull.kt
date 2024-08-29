package lib

import org.openrndr.math.Vector2

fun convexHull(p: List<Vector2>): List<Vector2> {
    if (p.isEmpty()) return emptyList()

    val sortedPoints = p.sortedBy { it.x }


    val h = mutableListOf<Vector2>()

    // lower hull
    for (pt in sortedPoints) {
        while (h.size >= 2 && !ccw(h[h.size - 2], h.last(), pt)) {
            h.removeAt(h.lastIndex)
        }
        h.add(pt)
    }

    // upper hull
    val t = h.size + 1
    for (i in sortedPoints.size - 2 downTo 0) {
        val pt = sortedPoints[i]
        while (h.size >= t && !ccw(h[h.size - 2], h.last(), pt)) {
            h.removeAt(h.lastIndex)
        }
        h.add(pt)
    }

    h.removeAt(h.lastIndex)
    return h
}

/* ccw returns true if the three points make a counter-clockwise turn */
fun ccw(a: Vector2, b: Vector2, c: Vector2) =
    ((b.x - a.x) * (c.y - a.y)) > ((b.y - a.y) * (c.x - a.x))
