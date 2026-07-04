package com.farmanimaltimer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmanimaltimer.logic.TimeMath
import com.farmanimaltimer.model.TimerPhase
import com.farmanimaltimer.model.TimerUiState

@Composable
fun CountdownScreen(
    state: TimerUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onStopAlert: () -> Unit,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = state.revealFraction,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
        label = "reveal",
    )
    val done = state.phase == TimerPhase.DONE

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(state.animal.displayName, fontSize = 22.sp)
        Spacer(Modifier.height(16.dp))

        PieReveal(
            animal = state.animal,
            revealFraction = if (done) 1f else animatedFraction,
            diameter = 260.dp,
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (done) "Time's up!" else TimeMath.format(state.remainingSeconds),
            fontSize = if (done) 34.sp else 44.sp,
            fontFamily = FontFamily.Monospace,
            color = if (done) Color(0xFF2E9E5B) else Color.Black,
        )

        Spacer(Modifier.height(28.dp))

        if (done) {
            Button(
                onClick = onStopAlert,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
            ) { Text("Stop", fontSize = 20.sp) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDecrement) { Text("− time", fontSize = 16.sp) }
                OutlinedButton(onClick = onIncrement) { Text("+ time", fontSize = 16.sp) }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.phase == TimerPhase.PAUSED) {
                    Button(onClick = onResume) { Text("Resume") }
                } else {
                    Button(onClick = onPause) { Text("Pause") }
                }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
