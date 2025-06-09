package com.example.v02.ReelsBlockingService

import android.content.Context
import com.example.v02.ReelsBlockingService.DataStoreProvider
import kotlinx.coroutines.flow.Flow

class DataStoreManager(context: Context) {
    private val dataStore = DataStoreProvider.getInstance(context)

    val appSettings: Flow<AppSettings> = dataStore.data

    suspend fun toggleInstagramReelsBlocking(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                instagram = currentSettings.instagram.copy(reelsBlocked = enabled)
            )
        }
    }
}