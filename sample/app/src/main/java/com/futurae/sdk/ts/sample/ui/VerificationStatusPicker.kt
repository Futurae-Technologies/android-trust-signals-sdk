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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.futurae.sdk.ts.model.public.TSVerificationStatus
import com.futurae.sdk.ts.sample.utils.UITestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStatusPicker(
    selected: TSVerificationStatus,
    onSelect: (TSVerificationStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = TSVerificationStatus.entries
    Column(modifier = modifier) {
        Text(
            text = "Verification Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = UITestTags.VerificationStatusOption.tag },
        ) {
            options.forEachIndexed { index, status ->
                SegmentedButton(
                    selected = status == selected,
                    onClick = { onSelect(status) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size,
                    ),
                    label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.semantics {
                        testTag = "${UITestTags.VerificationStatusOption.tag}_${status.name.lowercase()}"
                    },
                )
            }
        }
    }
}
