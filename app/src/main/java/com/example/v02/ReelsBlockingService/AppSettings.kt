package com.example.v02.ReelsBlockingService

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val instagram: App = App() 
)

@Serializable
enum class BlockMode {
    NONE,
    REELS,
    STORIES
}

@Serializable
data class App(
    val reelsBlocked: Boolean = false,
    val storiesBlocked: Boolean = false,
    val exploreBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)