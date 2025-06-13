package com.example.v02.ReelsBlockingService

import android.content.Context
import com.example.v02.ReelsBlockingService.DataStoreProvider
import kotlinx.coroutines.flow.Flow

class DataStoreManager(context: Context) {
    private val dataStore = DataStoreProvider.getInstance(context)

    val appSettings: Flow<AppSettings> = dataStore.data

    suspend fun setInstagramReelsBlocking(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                instagram = currentSettings.instagram.copy(reelsBlocked = enabled)
            )
        }
    }

    suspend fun setInstagramStoriesBlocking(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                instagram = currentSettings.instagram.copy(storiesBlocked = enabled)
            )
        }
    }

    suspend fun toggleInstagramExploreBlocking(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                instagram = currentSettings.instagram.copy(exploreBlocked = enabled)
            )
        }
    }

}
