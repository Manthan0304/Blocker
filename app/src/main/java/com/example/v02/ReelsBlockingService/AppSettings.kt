package com.example.v02.ReelsBlockingService

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val instagram: App = App() 
)

@Serializable
data class App(
    val reelsBlocked: Boolean = false,
    val blockedStart: Int = 0,
    val blockedEnd: Int = 1439
)
 
