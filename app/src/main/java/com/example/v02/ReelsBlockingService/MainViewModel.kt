package com.example.v02.ReelsBlockingService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    val isReelsBlockingEnabled: Flow<Boolean> = dataStoreManager.appSettings.map { settings ->
        settings.instagram.reelsBlocked
    }

    fun setReelsBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.toggleInstagramReelsBlocking(enabled)
        }
    }
}