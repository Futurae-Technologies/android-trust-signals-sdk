package com.futurae.sdk.ts.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSCollection
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration

private val prettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    explicitNulls = false
}

class SampleViewModel : ViewModel() {

    sealed interface CollectState {
        data object Idle : CollectState
        data object Loading : CollectState
        data class Success(val json: String) : CollectState
        data class Error(val message: String) : CollectState
    }

    sealed interface ScheduleState {
        data object None : ScheduleState
        data class Active(val accountIds: List<String>, val interval: Duration) : ScheduleState
        data class Error(val message: String) : ScheduleState
    }

    var collectState by mutableStateOf<CollectState>(CollectState.Idle)
        private set

    var scheduleState by mutableStateOf<ScheduleState>(ScheduleState.None)
        private set

    fun collectAndUpload(requests: List<TSCollectionRequest>) {
        collectState = CollectState.Loading
        viewModelScope.launch {
            collectState = try {
                val result: TSCollection = TrustSignalsSDK.collectAndUpload(*requests.toTypedArray())
                CollectState.Success(prettyJson.encodeToString(result))
            } catch (e: Exception) {
                CollectState.Error(e.message ?: e.toString())
            }
        }
    }

    fun scheduleCollection(requests: List<TSCollectionRequest>, interval: Duration) {
        scheduleState = try {
            TrustSignalsSDK.scheduleCollections(interval, *requests.toTypedArray())
            ScheduleState.Active(requests.map { it.accountId }, interval)
        } catch (e: IllegalArgumentException) {
            ScheduleState.Error(e.message ?: e.toString())
        }
    }

    fun stopSchedule() {
        TrustSignalsSDK.stopScheduledCollections()
        scheduleState = ScheduleState.None
    }
}
