package com.example.v02.ReelsBlockingService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    // Instagram flows
    val isReelsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.reelsBlocked
    }

    val isStoriesBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.storiesBlocked
    }

    val isExploreBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.exploreBlocked
    }

    // Facebook flows
    val isFBReelsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.reelsBlocked
    }

    val isFBMarketplaceBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.marketplaceBlocked
    }

    val isFBStoriesBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.storiesBlocked
    }

    // YouTube flows
    val isYTShortsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.youtube.shortsBlocked
    }

    val isYTCommentsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.youtube.commentsBlocked
    }

    val isYTSearchBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.youtube.searchBlocked
    }

    // Twitter flows
    val isTwitterExploreBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.twitter.exploreBlocked
    }

    // WhatsApp flows
    val isWhatsAppStatusBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.whatsapp.statusBlocked
    }

    // Snapchat flows
    val isSnapchatSpotlightBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.snapchat.spotlightBlocked
    }

    val isSnapchatStoriesBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.snapchat.storiesBlocked
    }

    // Instagram methods
    fun setReelsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setInstagramReelsBlocking(enabled)
        }
    }

    fun setStoriesBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setInstagramStoriesBlocking(enabled)
        }
    }

    fun setExploreBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.toggleInstagramExploreBlocking(enabled)
        }
    }

    fun setInstagramBlockTime(start: Int, end: Int) {
        viewModelScope.launch {
            dataStoreManager.setInstagramBlockTime(start, end)
        }
    }

    // Facebook methods
    fun setFBReelsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookReelsBlocking(enabled)
        }
    }

    fun setFBMarketplaceBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookMarketplaceBlocking(enabled)
        }
    }

    fun setFBStoriesBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookStoriesBlocking(enabled)
        }
    }

    // YouTube methods
    fun setYTShortsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setYouTubeShortsBlocking(enabled)
        }
    }

    fun setYTCommentsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setYouTubeCommentsBlocking(enabled)
        }
    }

    fun setYTSearchBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setYouTubeSearchBlocking(enabled)
        }
    }

    // Twitter methods
    fun setTwitterExploreBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setTwitterExploreBlocking(enabled)
        }
    }

    // WhatsApp methods
    fun setWhatsAppStatusBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setWhatsAppStatusBlocking(enabled)
        }
    }

    // Snapchat methods
    fun setSnapchatSpotlightBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSnapchatSpotlightBlocking(enabled)
        }
    }

    fun setSnapchatStoriesBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSnapchatStoriesBlocking(enabled)
        }
    }
}