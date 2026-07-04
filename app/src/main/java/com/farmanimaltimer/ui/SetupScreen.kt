package com.farmanimaltimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmanimaltimer.model.Animal

@Composable
fun SetupScreen(
    setup: SetupState,
    onSelectAnimal: (Animal) -> Unit,
    onSetFields: (Int, Int, Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Farm Animal Timer", fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))
        Text("Pick an animal", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Animal.entries) { animal ->
                val selected = animal == setup.animal
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color(0xFFDFF3E6) else Color(0xFFF2F2F2))
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) Color(0xFF2E9E5B) else Color(0xFFDDDDDD),
                            shape = CircleShape
                        )
                        .clickable { onSelectAnimal(animal) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (animal.hasPhoto) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(com.farmanimaltimer.R.drawable.ava),
                            contentDescription = animal.displayName,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                        )
                    } else {
                        androidx.compose.foundation.Canvas(Modifier.size(56.dp)) {
                            drawAnimal(animal, colorful = true)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(setup.animal.displayName, fontSize = 18.sp)

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeField("H", setup.hours) { onSetFields(it, setup.minutes, setup.seconds) }
            Colon()
            TimeField("M", setup.minutes) { onSetFields(setup.hours, it, setup.seconds) }
            Colon()
            TimeField("S", setup.seconds) { onSetFields(setup.hours, setup.minutes, it) }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onDecrement) { Text("− time", fontSize = 18.sp) }
            OutlinedButton(onClick = onIncrement) { Text("+ time", fontSize = 18.sp) }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Vibrate when time's up", fontSize = 16.sp)
            Switch(checked = setup.vibrate, onCheckedChange = onToggleVibrate)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onStart,
            enabled = setup.totalSeconds > 0,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) { Text("Start", fontSize = 20.sp) }
    }
}

@Composable
private fun Colon() = Text(":", fontSize = 34.sp, modifier = Modifier.padding(horizontal = 6.dp))

@Composable
private fun TimeField(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(2)
                onChange(digits.toIntOrNull() ?: 0)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 30.sp, textAlign = TextAlign.Center),
            modifier = Modifier.width(84.dp),
        )
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
