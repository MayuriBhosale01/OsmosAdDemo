package com.Mayuri.osmosaddemo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Mayuri.osmosaddemo.data.repository.AdRepository
import com.Mayuri.osmosaddemo.domain.model.AdResult
import com.Mayuri.osmosaddemo.domain.model.BannerAd
import com.Mayuri.osmosaddemo.utils.AdLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdViewModel(
    private val repository: AdRepository = AdRepository()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class AdLoaded(val ad: BannerAd) : UiState()
        data class Empty(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var impressionFired = false
    private var isFetching = false
    private val maxRetries = 3

    fun loadAd() {
        if (isFetching) return
        isFetching = true
        impressionFired = false

        viewModelScope.launch {
            fetchWithRetry()
        }
    }

    private suspend fun fetchWithRetry() {
        _uiState.value = UiState.Loading

        for (attempt in 0 until maxRetries) {
            when (val result = repository.fetchBannerAd()) {
                is AdResult.Success -> {
                    AdLogger.log("Ad Loaded ✅")
                    _uiState.value = UiState.AdLoaded(result.ad)
                    isFetching = false
                    return
                }
                is AdResult.Empty -> {
                    AdLogger.log("No Ad available")
                    _uiState.value = UiState.Empty(result.message)
                    isFetching = false
                    return
                }
                is AdResult.Failure -> {
                    if (attempt < maxRetries - 1) {
                        AdLogger.log("Retry ${attempt + 1}/$maxRetries...")
                        delay(1000L * (attempt + 1))
                    } else {
                        AdLogger.logError("Ad Failed after $maxRetries retries")
                        _uiState.value = UiState.Error(result.error)
                        isFetching = false
                    }
                }
            }
        }
    }

    fun fireImpression(ad: BannerAd) {
        if (impressionFired) return
        impressionFired = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // TODO: Replace with actual SDK method
                // OsmosSDK.registerImpressionEvent(impressionTrackingUrl = ad.impressionTrackingUrl)
                AdLogger.log("Impression Fired ✅")
            } catch (e: Exception) {
                AdLogger.logError("Impression failed: ${e.message}")
            }
        }
    }

    fun fireClick(ad: BannerAd) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // TODO: Replace with actual SDK method
                // OsmosSDK.registerClickEvent(clickTrackingUrl = ad.clickTrackingUrl)
                AdLogger.log("Click Fired ✅")
            } catch (e: Exception) {
                AdLogger.logError("Click tracking failed: ${e.message}")
            }
        }
    }
}