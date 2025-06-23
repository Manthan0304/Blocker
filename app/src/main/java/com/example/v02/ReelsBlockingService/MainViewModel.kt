package com.example.v02.ReelsBlockingService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    val isReelsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.reelsBlocked
    }

    val isStoriesBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.storiesBlocked
    }

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
    val isExploreBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.instagram.exploreBlocked
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
    val isFBReelsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.reelsBlocked
    }

    fun setFBReelsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookReelsBlocking(enabled)
        }
    }
    val isFBMarketplaceBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.marketplaceBlocked
    }

    fun setFBMarketplaceBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookMarketplaceBlocking(enabled)
        }
    }

    val isFBStoriesBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map {
        it.facebook.storiesBlocked
    }

    fun setFBStoriesBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setFacebookStoriesBlocking(enabled)
        }
    }
}
