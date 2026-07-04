package com.farmanimaltimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import com.farmanimaltimer.model.Animal

/**
 * Circular reveal: muted animal underneath, colorful animal clipped to a
 * clockwise pie sweep proportional to [revealFraction] (0f..1f).
 */
@Composable
fun PieReveal(
    animal: Animal,
    revealFraction: Float,
    diameter: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(diameter)) {
        // Backdrop circle so the art sits on a clean field.
        drawCircle(color = Color(0xFFEFF6F1))

        // Muted (not-yet-revealed) animal.
        drawAnimal(animal, colorful = false)

        val frac = revealFraction.coerceIn(0f, 1f)
        if (frac <= 0f) return@Canvas

        if (frac >= 1f) {
            drawAnimal(animal, colorful = true)
            return@Canvas
        }

        // Pie wedge path starting at 12 o'clock (-90°), sweeping clockwise.
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width // large enough to cover the whole canvas
        val path = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx - radius, top = cy - radius,
                    right = cx + radius, bottom = cy + radius
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 360f * frac,
                forceMoveTo = false
            )
            close()
        }
        clipPath(path) {
            drawAnimal(animal, colorful = true)
        }
    }
}
