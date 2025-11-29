package dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(values: List<Double?>, color: Color) {
    val pts = values.withIndex().mapNotNull { (i, v) ->
        if (v == null) null else i.toFloat() to v.toFloat()
    }
    if (pts.size < 2) {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {}
        return
    }

    val xs = pts.map { it.first }
    val ys = pts.map { it.second }
    val minX = xs.min()
    val maxX = xs.max()
    val minY = ys.min()
    val maxY = ys.max()

    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val w = size.width
        val h = size.height

        fun mx(x: Float) = (x - minX) / (maxX - minX) * w
        fun my(y: Float) = h - (y - minY) / (maxY - minY) * h

        for (i in 0 until pts.size - 1) {
            val (x1, y1) = pts[i]
            val (x2, y2) = pts[i + 1]
            drawLine(
                color,
                Offset(mx(x1), my(y1)),
                Offset(mx(x2), my(y2)),
                strokeWidth = 2f
            )
        }
    }
}