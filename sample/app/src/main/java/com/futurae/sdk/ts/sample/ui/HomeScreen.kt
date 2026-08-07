package com.futurae.sdk.ts.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sdk.ts.sample.SampleViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private enum class Action { CollectAndUpload, Schedule }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: SampleViewModel = viewModel()) {
    var pendingAction by remember { mutableStateOf<Action?>(null) }
    var selectedInterval by remember { mutableStateOf<Duration>(30.minutes) }

    pendingAction?.let { action ->
        CredentialsDialog(
            title = if (action == Action.CollectAndUpload) "Collect & Upload" else "Schedule Collection",
            onConfirm = { requests ->
                when (action) {
                    Action.CollectAndUpload -> vm.collectAndUpload(requests)
                    Action.Schedule -> vm.scheduleCollection(requests, selectedInterval)
                }
                pendingAction = null
            },
            onDismiss = { pendingAction = null },
            extraContent = if (action == Action.Schedule) {
                {
                    FrequencyPicker(
                        selected = selectedInterval,
                        onSelect = { selectedInterval = it },
                    )
                }
            } else null,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trust Signals Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionButtons(
                isLoading = vm.collectState is SampleViewModel.CollectState.Loading,
                onCollectAndUpload = { pendingAction = Action.CollectAndUpload },
                onSchedule = { pendingAction = Action.Schedule },
            )
            HorizontalDivider()
            ScheduledBanner(state = vm.scheduleState, onStop = vm::stopSchedule)
            CollectResultPanel(
                state = vm.collectState,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isLoading: Boolean,
    onCollectAndUpload: () -> Unit,
    onSchedule: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Actions", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onCollectAndUpload,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Collect & Upload") }
        OutlinedButton(
            onClick = onSchedule,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Schedule Collection") }
    }
}
