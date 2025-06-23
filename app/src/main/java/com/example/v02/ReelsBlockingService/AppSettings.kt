package com.example.v02.ReelsBlockingService

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val instagram: App = App(),
    val facebook: App = App()
)

@Serializable
data class App(
    val reelsBlocked: Boolean = false,
    val storiesBlocked: Boolean = false,
    val exploreBlocked: Boolean = false,
    val marketplaceBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)

@Serializable
enum class BlockMode {
    NONE,
    REELS,
    STORIES
}
