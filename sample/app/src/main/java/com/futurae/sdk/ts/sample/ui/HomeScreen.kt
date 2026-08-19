package com.futurae.sdk.ts.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sdk.ts.model.public.TSCollection
import com.futurae.sdk.ts.sample.SampleViewModel
import com.futurae.sdk.ts.sample.utils.UITestTags
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun Long.toDateTimeString(): String =
    SimpleDateFormat("MMM d, yyyy 'at' h:mm:ss a", Locale.getDefault()).format(Date(this * 1000))

private fun Double.toTruncated(): String {
    val s = "%.6f".format(this)
    return if (s.length > 7) "${s.take(3)}…${s.takeLast(3)}" else s
}

@Composable
fun HomeScreen(vm: SampleViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Trust Signals",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = vm::collectNow,
                        enabled = !vm.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = UITestTags.CollectNowButton.tag },
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Collect Now")
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    TextButton(
                        onClick = vm::collectAndUpload,
                        enabled = !vm.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = UITestTags.CollectAndUploadButton.tag },
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Collect & Upload")
                    }
                }

                HorizontalDivider()

                val fieldColors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )

                TextField(
                    value = vm.appId,
                    onValueChange = { vm.appId = it },
                    placeholder = { Text("App ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = UITestTags.AppIDInput.tag },
                    colors = fieldColors,
                    singleLine = true,
                )

                HorizontalDivider()

                TextField(
                    value = vm.accountIds,
                    onValueChange = { vm.accountIds = it },
                    placeholder = { Text("Account IDs (comma separated)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = UITestTags.AccountIDInput.tag },
                    colors = fieldColors,
                    singleLine = true,
                )

                HorizontalDivider()

                TextField(
                    value = vm.accessToken,
                    onValueChange = { vm.accessToken = it },
                    placeholder = { Text("OAuth2 access token") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = UITestTags.AccessTokenInput.tag },
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )

                FrequencyPicker(
                    selected = vm.selectedFrequency,
                    onSelect = vm::setFrequency,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        Text(
            text = "Collections (${vm.collections.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.semantics { testTag = UITestTags.CollectionsCount.tag },
        )
        Text(
            text = "A scheduled cycle reports only failures, so a row with no result means it was not rejected.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (vm.collections.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().semantics { testTag = UITestTags.CollectionEmptyState.tag }) {
                Text(
                    text = "No collections yet — tap Collect Now",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = UITestTags.CollectionList.tag },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                vm.collections.forEachIndexed { index, entry ->
                    CollectionEntryCard(
                        entry = entry,
                        onSelect = vm::selectEntry,
                        modifier = Modifier.semantics { testTag = "${UITestTags.CollectionItem.tag}_$index" },
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionEntryCard(
    entry: SampleViewModel.CollectionEntry,
    onSelect: (SampleViewModel.CollectionEntry.Success) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (entry) {
        is SampleViewModel.CollectionEntry.Success -> Card(
            onClick = { onSelect(entry) },
            modifier = modifier.fillMaxWidth(),
        ) {
            CollectionSuccessContent(entry)
        }
        is SampleViewModel.CollectionEntry.Error -> Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun CollectionSuccessContent(entry: SampleViewModel.CollectionEntry.Success) {
    val collection = entry.collection
    val obs = collection.observation
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = collection.timestamp.toDateTimeString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
            TagBadge(collection.tag)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SignalStat("BLE", obs.bleScan?.permission, obs.bleScan?.scanResults?.size?.toString() ?: "0")
            SignalStat("WiFi", obs.wifiScan?.permission, obs.wifiScan?.scanResults?.size?.toString() ?: "—")
            SignalStat(
                label = "Loc",
                permitted = obs.locationCollection != null,
                value = obs.locationCollection?.lat?.toTruncated() ?: "—",
            )
            SignalStat("Near", null, obs.nearbyDevices?.size?.toString() ?: "—")
            SignalStat("Call", null, if (obs.activeCall == true) "yes" else "no")
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "id ${entry.id.take(8)}…",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (entry.uploaded) Icons.Default.Done else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (entry.uploaded) MaterialTheme.colorScheme.tertiary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (entry.uploaded) "uploaded successfully" else "collect only — never uploaded",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SignalStat(label: String, permitted: Boolean?, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (permitted != null) {
                Icon(
                    imageVector = if (permitted) Icons.Default.Done else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = if (permitted) MaterialTheme.colorScheme.tertiary
                           else MaterialTheme.colorScheme.error,
                )
            }
        }
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TagBadge(tag: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = tag.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
