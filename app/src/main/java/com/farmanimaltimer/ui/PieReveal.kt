package com.farmanimaltimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import com.farmanimaltimer.model.Animal

/**
 * Circular reveal. The full-color animal is drawn inside a circle, then a SOLID,
 * fully opaque cover hides it completely. As [revealFraction] grows 0f..1f, a
 * clockwise pie wedge of that cover is removed (starting at 12 o'clock),
 * uncovering the animal beneath. At 1f the animal is fully revealed.
 */
@Composable
fun PieReveal(
    animal: Animal,
    revealFraction: Float,
    diameter: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(diameter)) {
        val w = size.width
        val h = size.height
        val circle = Path().apply { addOval(Rect(0f, 0f, w, h)) }

        clipPath(circle) {
            // Opaque field + full-color animal underneath the cover.
            drawRect(color = Color(0xFFFFFFFF))
            drawAnimal(animal, colorful = true)

            val frac = revealFraction.coerceIn(0f, 1f)
            if (frac >= 1f) return@clipPath

            val cover = Color(0xFF7EC8A0) // solid, fully opaque cover
            if (frac <= 0f) {
                drawRect(color = cover) // entire animal hidden
                return@clipPath
            }

            // Cover the still-hidden wedge: from where the reveal ends, sweeping the remainder.
            val cx = w / 2f
            val cy = h / 2f
            val radius = w // oversize so the wedge fully spans the circle
            val hiddenWedge = Path().apply {
                moveTo(cx, cy)
                arcTo(
                    rect = Rect(cx - radius, cy - radius, cx + radius, cy + radius),
                    startAngleDegrees = -90f + 360f * frac,
                    sweepAngleDegrees = 360f * (1f - frac),
                    forceMoveTo = false
                )
                close()
            }
            clipPath(hiddenWedge) {
                drawRect(color = cover)
            }
        }
    }
}
