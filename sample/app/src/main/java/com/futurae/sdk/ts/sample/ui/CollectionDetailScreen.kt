package com.futurae.sdk.ts.sample.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.json.Json
import androidx.compose.ui.unit.dp
import com.futurae.sdk.ts.sample.SampleViewModel
import com.futurae.sdk.ts.sample.utils.UITestTags
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun Long.toTimeString(): String =
    SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(this * 1000))

private fun Long.toDateTimeString(): String =
    SimpleDateFormat("MMM d, yyyy 'at' h:mm:ss a", Locale.getDefault()).format(Date(this * 1000))

@Composable
fun CollectionDetailScreen(
    entry: SampleViewModel.CollectionEntry.Success,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }

    val collection = entry.collection
    val obs = collection.observation
    val rawJson = remember(entry.id) {
        Json { prettyPrint = true; encodeDefaults = true; explicitNulls = false }
            .encodeToString(collection)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.semantics { testTag = UITestTags.CollectionDetailBackButton.tag },
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Text(
            text = collection.timestamp.toTimeString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        // ── Header ────────────────────────────────────────────────────────────

        SectionLabel("Header")
        KeyValueCard {
            KeyValueRow("timestamp", collection.timestamp.toString())
            HorizontalDivider()
            KeyValueRow("date", collection.timestamp.toDateTimeString())
            HorizontalDivider()
            KeyValueRow("tag", collection.tag)
            HorizontalDivider()
            KeyValueRow("collectionId", entry.id)
        }

        // ── observation.wifiScan ──────────────────────────────────────────────

        obs.wifiScan?.let { wifiScan ->
            SectionLabel("observation.wifiScan")
            KeyValueCard {
                PermissionRow(wifiScan.permission)
                HorizontalDivider()
                if (wifiScan.scanResults.isEmpty()) {
                    InfoRow("no scan results")
                } else {
                    wifiScan.scanResults.forEachIndexed { i, result ->
                        if (i > 0) HorizontalDivider()
                        KeyValueRow(result.name ?: "unknown", result.address)
                    }
                }
            }
        }

        // ── observation.bleScan ───────────────────────────────────────────────

        obs.bleScan?.let { bleScan ->
            SectionLabel("observation.bleScan")
            KeyValueCard {
                PermissionRow(bleScan.permission)
                HorizontalDivider()
                if (bleScan.scanResults.isEmpty()) {
                    InfoRow("no peripherals discovered")
                } else {
                    bleScan.scanResults.forEachIndexed { i, result ->
                        if (i > 0) HorizontalDivider()
                        KeyValueRow(result.name ?: "unknown", result.address)
                    }
                }
            }
        }

        // ── observation.locationCollection ────────────────────────────────────

        obs.locationCollection?.let { loc ->
            SectionLabel("observation.locationCollection")
            KeyValueCard {
                KeyValueRow("lat", loc.lat.toString())
                HorizontalDivider()
                KeyValueRow("lon", loc.lon.toString())
                loc.accuracy?.let {
                    HorizontalDivider()
                    KeyValueRow("accuracy", it.toString())
                }
                loc.speed?.let {
                    HorizontalDivider()
                    KeyValueRow("speed", it.toString())
                }
                HorizontalDivider()
                KeyValueRow("timestamp", loc.timestamp.toString())
            }
        }

        // ── observation.wifiNetwork ───────────────────────────────────────────

        obs.wifiNetwork?.let { net ->
            SectionLabel("observation.wifiNetwork")
            KeyValueCard {
                KeyValueRow("name", net.name ?: "—")
                net.bssid?.let {
                    HorizontalDivider()
                    KeyValueRow("bssid", it)
                }
                net.rssi?.let {
                    HorizontalDivider()
                    KeyValueRow("rssi", it.toString())
                }
                HorizontalDivider()
                KeyValueRow("timestamp", net.timestamp.toString())
                net.connectedDevices?.takeIf { it.isNotEmpty() }?.let { devices ->
                    devices.forEachIndexed { i, device ->
                        HorizontalDivider()
                        KeyValueRow(device.name ?: "unknown", device.address)
                    }
                }
            }
        }

        // ── observation.blePeripherals ────────────────────────────────────────

        obs.blePeripherals?.let { ble ->
            SectionLabel("observation.blePeripherals")
            KeyValueCard {
                PermissionRow(ble.permission)
                HorizontalDivider()
                if (ble.connectedBLEs.isEmpty()) {
                    InfoRow("no peripherals connected")
                } else {
                    ble.connectedBLEs.forEachIndexed { i, device ->
                        if (i > 0) HorizontalDivider()
                        KeyValueRow(device.name ?: "unknown", device.address)
                    }
                }
            }
        }

        // ── observation.nearbyDevices ─────────────────────────────────────────

        obs.nearbyDevices?.let { devices ->
            SectionLabel("observation.nearbyDevices")
            KeyValueCard {
                if (devices.isEmpty()) {
                    InfoRow("no nearby devices discovered")
                } else {
                    devices.forEachIndexed { i, device ->
                        if (i > 0) HorizontalDivider()
                        KeyValueRow(device.name ?: "unknown", device.address)
                    }
                }
            }
        }

        // ── observation.ip ────────────────────────────────────────────────────

        obs.ip?.let { ip ->
            SectionLabel("observation.ip")
            KeyValueCard {
                KeyValueRow("address", ip)
            }
        }

        // ── observation.timezone ──────────────────────────────────────────────

        SectionLabel("observation.timezone")
        KeyValueCard {
            KeyValueRow("id", obs.timezone.id)
            HorizontalDivider()
            KeyValueRow("name", obs.timezone.name)
        }

        // ── observation.device ────────────────────────────────────────────────

        obs.device?.let { device ->
            SectionLabel("observation.device")
            KeyValueCard {
                KeyValueRow("model", device.model)
                HorizontalDivider()
                KeyValueRow("manufacturer", device.manufacturer)
                HorizontalDivider()
                KeyValueRow("device", device.device)
                HorizontalDivider()
                KeyValueRow("screenResolution", device.screenResolution)
                HorizontalDivider()
                KeyValueRow("screenDensityDpi", device.screenDensityDpi.toString())
                HorizontalDivider()
                KeyValueRow("androidVersion", device.androidVersion)
                HorizontalDivider()
                KeyValueRow("sdkInt", device.sdkInt.toString())
                HorizontalDivider()
                KeyValueRow("uptimeMs", device.uptimeMs.toString())
            }
        }

        // ── observation.locale ────────────────────────────────────────────────

        obs.locale?.let { locale ->
            SectionLabel("observation.locale")
            KeyValueCard {
                KeyValueRow("language", locale.language)
                HorizontalDivider()
                KeyValueRow("region", locale.region)
            }
        }

        // ── observation.networkState ──────────────────────────────────────────

        obs.networkState?.let { ns ->
            SectionLabel("observation.networkState")
            KeyValueCard {
                KeyValueRow("networkType", ns.networkType)
                HorizontalDivider()
                KeyValueRow("vpnActive", ns.vpnActive.toString())
                HorizontalDivider()
                KeyValueRow("proxyHost", ns.proxyHost ?: "none")
            }
        }

        // ── observation.activeCall ────────────────────────────────────────────

        obs.activeCall?.let { activeCall ->
            SectionLabel("observation.activeCall")
            KeyValueCard {
                KeyValueRow("active", if (activeCall) "yes" else "no")
            }
        }

        // ── observation.security ──────────────────────────────────────────────

        obs.security?.let { security ->
            SectionLabel("observation.security")
            KeyValueCard {
                KeyValueRow("debuggerAttached", security.debuggerAttached.toString())
                HorizontalDivider()
                KeyValueRow("developerModeEnabled", security.developerModeEnabled.toString())
            }
        }

        // ── observation.app ───────────────────────────────────────────────────

        obs.app?.let { app ->
            SectionLabel("observation.app")
            KeyValueCard {
                KeyValueRow("version", app.version ?: "—")
                HorizontalDivider()
                KeyValueRow("installerPackage", app.installerPackage ?: "—")
            }
        }

        // ── observation.battery ───────────────────────────────────────────────

        obs.battery?.let { battery ->
            SectionLabel("observation.battery")
            KeyValueCard {
                KeyValueRow("level", "${battery.level}%")
                HorizontalDivider()
                KeyValueRow("status", battery.status)
            }
        }

        // ── Raw JSON ──────────────────────────────────────────────────────────

        val clipboardManager = LocalClipboardManager.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Raw JSON")
            TextButton(
                onClick = { clipboardManager.setText(AnnotatedString(rawJson)) },
                modifier = Modifier.semantics { testTag = UITestTags.CopyJsonButton.tag },
            ) {
                Text("Copy")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = rawJson,
                modifier = Modifier
                    .padding(12.dp)
                    .semantics { testTag = UITestTags.RawJsonText.tag },
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// ── Helper composables ─────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )
}

@Composable
private fun KeyValueCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), content = content)
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun PermissionRow(permitted: Boolean) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (permitted) Icons.Default.Done else Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (permitted) MaterialTheme.colorScheme.tertiary
                   else MaterialTheme.colorScheme.error,
        )
        Text(
            text = "permission: $permitted",
            style = MaterialTheme.typography.bodySmall,
            color = if (permitted) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
