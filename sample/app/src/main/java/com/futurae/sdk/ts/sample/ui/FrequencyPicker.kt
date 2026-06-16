package com.futurae.sdk.ts.sample.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal val scheduleIntervals = listOf(15.minutes, 30.minutes, 1.hours, 6.hours)

internal fun Duration.toLabel(): String = toComponents { hours, minutes, _, _ ->
    when {
        hours > 0 && minutes == 0 -> "${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyPicker(
    selected: Duration,
    onSelect: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Collection frequency",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            scheduleIntervals.forEachIndexed { index, interval ->
                SegmentedButton(
                    selected = interval == selected,
                    onClick = { onSelect(interval) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = scheduleIntervals.size,
                    ),
                    label = { Text(interval.toLabel()) },
                )
            }
        }
    }
}
