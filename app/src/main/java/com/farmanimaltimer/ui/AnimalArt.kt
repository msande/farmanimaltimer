package com.farmanimaltimer.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import com.farmanimaltimer.model.Animal

/**
 * Draws a flat illustration of [animal] filling the current DrawScope bounds.
 * When [colorful] is false, uses a muted grey palette (silhouette look).
 * All coordinates are fractions of size, so it scales to any canvas.
 */
fun DrawScope.drawAnimal(animal: Animal, colorful: Boolean) {
    val w = size.width
    val h = size.height
    fun c(full: Color): Color =
        if (colorful) full else Color(0xFF9E9E9E).copy(alpha = 0.55f)

    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) {
        drawOval(
            color = color,
            topLeft = Offset((cx - rx) * w, (cy - ry) * h),
            size = Size(rx * 2 * w, ry * 2 * h),
            style = Fill
        )
    }
    fun dot(cx: Float, cy: Float, r: Float, color: Color) =
        drawCircle(color, radius = r * minOf(w, h), center = Offset(cx * w, cy * h))

    when (animal) {
        Animal.COW -> {
            ellipse(0.5f, 0.58f, 0.34f, 0.28f, c(Color(0xFFF3E9E1)))   // body
            ellipse(0.5f, 0.44f, 0.20f, 0.17f, c(Color(0xFFFDF7F2)))   // head
            ellipse(0.5f, 0.50f, 0.13f, 0.10f, c(Color(0xFFF6BFC4)))   // snout
            ellipse(0.32f, 0.30f, 0.06f, 0.05f, c(Color(0xFF4A3728)))  // ear
            ellipse(0.68f, 0.30f, 0.06f, 0.05f, c(Color(0xFF4A3728)))
            dot(0.43f, 0.42f, 0.03f, c(Color(0xFF3A2A20)))            // eyes
            dot(0.57f, 0.42f, 0.03f, c(Color(0xFF3A2A20)))
            dot(0.46f, 0.51f, 0.018f, c(Color(0xFF8A4A50)))          // nostrils
            dot(0.54f, 0.51f, 0.018f, c(Color(0xFF8A4A50)))
            ellipse(0.30f, 0.62f, 0.09f, 0.10f, c(Color(0xFF5A4636))) // spot
        }
        Animal.PIG -> {
            ellipse(0.5f, 0.58f, 0.33f, 0.27f, c(Color(0xFFF7B4C4)))
            ellipse(0.5f, 0.44f, 0.20f, 0.17f, c(Color(0xFFF9C6D3)))
            ellipse(0.5f, 0.50f, 0.11f, 0.09f, c(Color(0xFFE79AAE)))
            dot(0.46f, 0.50f, 0.02f, c(Color(0xFF9C5A6E)))
            dot(0.54f, 0.50f, 0.02f, c(Color(0xFF9C5A6E)))
            ellipse(0.34f, 0.30f, 0.06f, 0.06f, c(Color(0xFFE79AAE)))
            ellipse(0.66f, 0.30f, 0.06f, 0.06f, c(Color(0xFFE79AAE)))
            dot(0.43f, 0.42f, 0.025f, c(Color(0xFF3A2A20)))
            dot(0.57f, 0.42f, 0.025f, c(Color(0xFF3A2A20)))
        }
        Animal.CHICKEN -> {
            ellipse(0.5f, 0.60f, 0.28f, 0.26f, c(Color(0xFFFFFFFF)))  // body
            ellipse(0.5f, 0.38f, 0.16f, 0.15f, c(Color(0xFFFFFFFF)))  // head
            ellipse(0.5f, 0.24f, 0.06f, 0.05f, c(Color(0xFFE23B3B)))  // comb
            ellipse(0.62f, 0.42f, 0.05f, 0.035f, c(Color(0xFFF6A623))) // beak
            dot(0.54f, 0.37f, 0.022f, c(Color(0xFF2A2A2A)))          // eye
            ellipse(0.5f, 0.50f, 0.05f, 0.04f, c(Color(0xFFE23B3B)))  // wattle
        }
        Animal.SHEEP -> {
            ellipse(0.5f, 0.58f, 0.34f, 0.29f, c(Color(0xFFF2F2F2)))  // wool body
            ellipse(0.5f, 0.46f, 0.15f, 0.15f, c(Color(0xFF3A3A3A)))  // face
            ellipse(0.36f, 0.44f, 0.05f, 0.07f, c(Color(0xFF3A3A3A))) // ears
            ellipse(0.64f, 0.44f, 0.05f, 0.07f, c(Color(0xFF3A3A3A)))
            dot(0.45f, 0.45f, 0.022f, c(Color(0xFFFFFFFF)))
            dot(0.55f, 0.45f, 0.022f, c(Color(0xFFFFFFFF)))
        }
        Animal.HORSE -> {
            ellipse(0.52f, 0.60f, 0.32f, 0.26f, c(Color(0xFF9B6A43)))  // body
            ellipse(0.40f, 0.40f, 0.15f, 0.20f, c(Color(0xFFA9764C)))  // head/neck
            ellipse(0.36f, 0.42f, 0.05f, 0.06f, c(Color(0xFF6E4A2E)))  // ear
            ellipse(0.32f, 0.30f, 0.10f, 0.10f, c(Color(0xFF4A3323)))  // mane
            dot(0.34f, 0.44f, 0.022f, c(Color(0xFF2A1E14)))           // eye
            ellipse(0.30f, 0.52f, 0.07f, 0.06f, c(Color(0xFF7A5334)))  // muzzle
        }
        Animal.DUCK -> {
            ellipse(0.52f, 0.60f, 0.30f, 0.24f, c(Color(0xFFF7D64B)))  // body
            ellipse(0.40f, 0.42f, 0.15f, 0.14f, c(Color(0xFFF9DE6A)))  // head
            ellipse(0.26f, 0.46f, 0.09f, 0.045f, c(Color(0xFFF3922B))) // bill
            dot(0.40f, 0.40f, 0.022f, c(Color(0xFF2A2A2A)))           // eye
            ellipse(0.62f, 0.60f, 0.14f, 0.12f, c(Color(0xFFEEC63C)))  // wing
        }
    }
}
