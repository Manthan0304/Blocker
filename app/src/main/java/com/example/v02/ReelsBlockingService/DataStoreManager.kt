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

    suspend fun setInstagramBlockTime(start: Int, end: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                instagram = currentSettings.instagram.copy(
                    blockedStart = start,
                    blockedEnd = end
                )
            )
        }
    }

    suspend fun setFacebookReelsBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                facebook = current.facebook.copy(reelsBlocked = enabled)
            )
        }
    }

    suspend fun setFacebookMarketplaceBlocking(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                facebook = currentSettings.facebook.copy(marketplaceBlocked = enabled)
            )
        }
    }

    suspend fun setFacebookStoriesBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                facebook = current.facebook.copy(storiesBlocked = enabled)
            )
        }
    }

    // YouTube methods
    suspend fun setYouTubeShortsBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                youtube = current.youtube.copy(shortsBlocked = enabled)
            )
        }
    }

    suspend fun setYouTubeCommentsBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                youtube = current.youtube.copy(commentsBlocked = enabled)
            )
        }
    }

    suspend fun setYouTubeSearchBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                youtube = current.youtube.copy(searchBlocked = enabled)
            )
        }
    }

    suspend fun setYouTubeBlockTime(start: Int, end: Int) {
        dataStore.updateData { current ->
            current.copy(
                youtube = current.youtube.copy(blockedStart = start, blockedEnd = end)
            )
        }
    }

    // Twitter methods
    suspend fun setTwitterExploreBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                twitter = current.twitter.copy(exploreBlocked = enabled)
            )
        }
    }

    suspend fun setTwitterBlockTime(start: Int, end: Int) {
        dataStore.updateData { current ->
            current.copy(
                twitter = current.twitter.copy(blockedStart = start, blockedEnd = end)
            )
        }
    }

    // WhatsApp methods
    suspend fun setWhatsAppStatusBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                whatsapp = current.whatsapp.copy(statusBlocked = enabled)
            )
        }
    }

    suspend fun setWhatsAppBlockTime(start: Int, end: Int) {
        dataStore.updateData { current ->
            current.copy(
                whatsapp = current.whatsapp.copy(blockedStart = start, blockedEnd = end)
            )
        }
    }

    // Snapchat methods
    suspend fun setSnapchatSpotlightBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                snapchat = current.snapchat.copy(spotlightBlocked = enabled)
            )
        }
    }

    suspend fun setSnapchatStoriesBlocking(enabled: Boolean) {
        dataStore.updateData { current ->
            current.copy(
                snapchat = current.snapchat.copy(storiesBlocked = enabled)
            )
        }
    }

    suspend fun setSnapchatBlockTime(start: Int, end: Int) {
        dataStore.updateData { current ->
            current.copy(
                snapchat = current.snapchat.copy(blockedStart = start, blockedEnd = end)
            )
        }
    }
}
