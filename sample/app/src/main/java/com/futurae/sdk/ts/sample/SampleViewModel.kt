package com.futurae.sdk.ts.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.error.TSUploadException
import com.futurae.sdk.ts.model.public.TSCollection
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SampleViewModel : ViewModel() {

    enum class ScheduleFrequency(val label: String, val duration: Duration?) {
        Off("Off", null),
        ThirtySeconds("30s", 30.seconds),
        ThreeMinutes("3m", 3.minutes),
        SevenMinutes("7m", 7.minutes),
    }

    sealed interface CollectionEntry {
        data class Success(
            val id: String,
            val collection: TSCollection,
            val uploaded: Boolean,
        ) : CollectionEntry
        data class Error(val message: String) : CollectionEntry
    }

    var appId by mutableStateOf("")
    var accountIds by mutableStateOf("")
    var accessToken by mutableStateOf("")

    var selectedFrequency by mutableStateOf(ScheduleFrequency.Off)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var collections by mutableStateOf<List<CollectionEntry>>(emptyList())
        private set

    var selectedEntry by mutableStateOf<CollectionEntry.Success?>(null)
        private set

    private var scheduleJob: Job? = null

    fun setFrequency(frequency: ScheduleFrequency) {
        selectedFrequency = frequency
        scheduleJob?.cancel()
        scheduleJob = null
        val duration = frequency.duration ?: return
        scheduleJob = viewModelScope.launch {
            while (isActive) {
                delay(duration)
                buildRequests()?.let { doCollectAndUpload(it) }
            }
        }
    }

    fun selectEntry(entry: CollectionEntry.Success) { selectedEntry = entry }
    fun clearSelection() { selectedEntry = null }

    fun collectNow() {
        viewModelScope.launch {
            isLoading = true
            val entry = try {
                val collection = TrustSignalsSDK.collect()
                CollectionEntry.Success(
                    id = UUID.randomUUID().toString(),
                    collection = collection,
                    uploaded = false,
                )
            } catch (e: Exception) {
                CollectionEntry.Error(e.message ?: e.toString())
            }
            collections = listOf(entry) + collections
            isLoading = false
        }
    }

    fun collectAndUpload() {
        val requests = buildRequests() ?: return
        viewModelScope.launch {
            isLoading = true
            doCollectAndUpload(requests)
            isLoading = false
        }
    }

    private suspend fun doCollectAndUpload(requests: List<TSCollectionRequest>) {
        val entry = try {
            val collection = TrustSignalsSDK.collectAndUpload(*requests.toTypedArray())
            CollectionEntry.Success(
                id = UUID.randomUUID().toString(),
                collection = collection,
                uploaded = true,
            )
        } catch (e: TSUploadException) {
            val detail = e.failures.entries.joinToString("\n") { (id, err) -> "$id: ${err.message}" }
            CollectionEntry.Error(detail)
        } catch (e: Exception) {
            CollectionEntry.Error(e.message ?: e.toString())
        }
        collections = listOf(entry) + collections
    }

    private fun buildRequests(): List<TSCollectionRequest>? {
        val ids = accountIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (ids.isEmpty() || accessToken.isBlank() || appId.isBlank()) return null
        return ids.map { TSCollectionRequest(accountId = it, accessToken = accessToken, appId = appId) }
    }
}
