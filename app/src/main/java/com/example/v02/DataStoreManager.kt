package com.example.v02

import android.content.Context
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
