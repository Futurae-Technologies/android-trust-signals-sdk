package com.futurae.sdk.ts.sample

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSConfiguration
import com.futurae.sdk.ts.sample.ui.CollectionDetailScreen
import com.futurae.sdk.ts.sample.ui.HomeScreen

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* collectors handle missing permissions gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TrustSignalsSDK.initialize(
            context = this,
            configuration = TSConfiguration(serverURL = BuildConfig.TS_BASE_URL),
        )

        permissionsLauncher.launch(buildRequiredPermissions())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                    AppContent()
                }
            }
        }
    }

    private fun buildRequiredPermissions(): Array<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        add(Manifest.permission.READ_PHONE_STATE)
    }.toTypedArray()
}

@Composable
private fun AppContent(vm: SampleViewModel = viewModel()) {
    val selected = vm.selectedEntry
    if (selected != null) {
        CollectionDetailScreen(entry = selected, onBack = vm::clearSelection)
    } else {
        HomeScreen(vm = vm)
    }
}
