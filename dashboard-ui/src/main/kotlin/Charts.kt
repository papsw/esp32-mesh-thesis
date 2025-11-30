package dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun LineChart(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
) {
    // Nothing useful to draw
    if (values.size < 2) return

    Canvas(modifier) {
        // ----- normalize to floats -----
        val minY = values.minOrNull()?.toFloat() ?: 0f
        val maxY = values.maxOrNull()?.toFloat() ?: 1f
        val rangeY = (maxY - minY).takeIf { it != 0f } ?: 1f

        val lastIndex = (values.size - 1).coerceAtLeast(1)

        // map index/value -> Offset in canvas space
        val pts: List<Offset> = values.mapIndexed { i, v ->
            val xNorm = i.toFloat() / lastIndex
            val yNorm = (v.toFloat() - minY) / rangeY

            Offset(
                x = xNorm * size.width,
                y = size.height - (yNorm * size.height)
            )
        }

        // ----- optional horizontal grid -----
        val gridColor = color.copy(alpha = 0.15f)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = size.height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        // ----- AREA FILL UNDER CURVE (poly under smooth-ish line) -----
        val fillPath = Path().apply {
            moveTo(pts.first().x, size.height)
            // follow points
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            color = color.copy(alpha = 0.25f)
        )

        // ----- SMOOTH CURVED LINE (quadratic Beziers) -----
        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)

            if (pts.size == 2) {
                // just a straight segment
                lineTo(pts[1].x, pts[1].y)
            } else {
                // smooth curve using midpoints as Bezier targets
                for (i in 1 until pts.size - 1) {
                    val p0 = pts[i]
                    val p1 = pts[i + 1]
                    val mid = Offset(
                        (p0.x + p1.x) / 2f,
                        (p0.y + p1.y) / 2f
                    )
                    quadraticBezierTo(
                        p0.x, p0.y,
                        mid.x, mid.y
                    )
                }
                // last segment to final point
                val beforeLast = pts[pts.size - 2]
                val last = pts.last()
                quadraticBezierTo(
                    beforeLast.x, beforeLast.y,
                    last.x, last.y
                )
            }
        }

        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 3f)
        )

        // ----- POINTS -----
        pts.forEach { p ->
            drawCircle(
                color = color,
                radius = 3f,
                center = p
            )
        }
    }
}