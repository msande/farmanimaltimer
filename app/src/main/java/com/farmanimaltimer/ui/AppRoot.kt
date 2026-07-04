package com.farmanimaltimer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmanimaltimer.model.TimerPhase

@Composable
fun AppRoot(vm: TimerViewModel = viewModel()) {
    val timer by vm.timerState.collectAsStateWithLifecycle()
    val setup by vm.setup.collectAsStateWithLifecycle()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (timer.phase == TimerPhase.IDLE) {
                SetupScreen(
                    setup = setup,
                    onSelectAnimal = vm::selectAnimal,
                    onSetFields = vm::setFields,
                    onIncrement = vm::incrementSetup,
                    onDecrement = vm::decrementSetup,
                    onStart = vm::start,
                )
            } else {
                CountdownScreen(
                    state = timer,
                    onIncrement = vm::incrementRunning,
                    onDecrement = vm::decrementRunning,
                    onPause = vm::pause,
                    onResume = vm::resume,
                    onCancel = vm::cancel,
                    onStopAlert = vm::stopAlert,
                )
            }
        }
    }
}
