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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSConfiguration
import com.futurae.sdk.ts.sample.ui.HomeScreen
import com.futurae.sdk.ts.sample.ui.SetupScreen

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* collectors handle missing permissions gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsLauncher.launch(buildRequiredPermissions())

        setContent {
            var sdkInitialized by remember { mutableStateOf(false) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (sdkInitialized) {
                        HomeScreen()
                    } else {
                        SetupScreen(
                            onConfirm = { appId ->
                                TrustSignalsSDK.initialize(
                                    context = this,
                                    configuration = TSConfiguration(
                                        appId = appId,
                                        serverURL = BuildConfig.TS_BASE_URL,
                                    ),
                                )
                                sdkInitialized = true
                            },
                        )
                    }
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
