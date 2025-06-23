package com.example.v02

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.util.Log
import com.example.v02.ReelsBlockingService.AppSettings
import com.example.v02.ReelsBlockingService.DataStoreManager
import java.util.TimeZone

private const val TAG = "ReelsBlockService"

class InstagramBlockAccessibilityService : AccessibilityService() {

    private lateinit var dataStore: DataStoreManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var settings = AppSettings()

    private var lastActionTime = 0L
    private val debounceMillis = 200L

    override fun onServiceConnected() {
        super.onServiceConnected()
        dataStore = DataStoreManager(this)

        serviceScope.launch {
            dataStore.appSettings.collect { latest ->
                settings = latest
                Log.d(TAG, "Settings updated: Instagram reels=${latest.instagram.reelsBlocked}, FB reels=${latest.facebook.reelsBlocked}, FB marketplace=${latest.facebook.marketplaceBlocked}")
            }
        }

        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return
        val nowMin = currentMinuteOfDay()

        Log.d(TAG, "Received event from package: $pkg")

        when (pkg) {
            "com.instagram.android" -> {
                val insta = settings.instagram
                if (isWithinInterval(insta.blockedStart, insta.blockedEnd, nowMin)) {
                    if (insta.reelsBlocked) blockInstagramReels(root)
                    if (insta.storiesBlocked) blockInstagramStories(root)
                    if (insta.exploreBlocked) blockInstagramExplore(root)
                }
            }

            "com.facebook.katana" -> {
                logAllDescriptions(root) // For debugging

                val fb = settings.facebook
                if (isWithinInterval(fb.blockedStart, fb.blockedEnd, nowMin)) {
                    if (fb.reelsBlocked) blockFacebookReels(root)
                    if (fb.marketplaceBlocked) blockFacebookMarketplace(root)
                }
            }
        }
    }

    private fun blockFacebookReels(root: AccessibilityNodeInfo) {
        // Check if we're on Video tab by looking for Video tab being selected
        val videoTab = findNodeByDesc(root, "Video")

        if (videoTab?.isSelected == true) {
            Log.d(TAG, "Facebook Video tab detected, redirecting to Home")
            val homeTab = findNodeByDesc(root, "Home")
            exitTheFacebookDoom(homeTab, "Facebook Video tab blocked")
        }
    }

    private fun blockFacebookMarketplace(root: AccessibilityNodeInfo) {
        // Check if we're on Marketplace tab by looking for Marketplace tab being selected
        val marketplaceTab = findNodeByDesc(root, "Marketplace")

        if (marketplaceTab?.isSelected == true) {
            Log.d(TAG, "Facebook Marketplace tab detected, redirecting to Home")
            val homeTab = findNodeByDesc(root, "Home")
            exitTheFacebookDoom(homeTab, "Facebook Marketplace blocked")
        }
    }

    private fun exitTheFacebookDoom(tab: AccessibilityNodeInfo?, reason: String) {
        val now = System.currentTimeMillis()
        if (now - lastActionTime < debounceMillis) return
        lastActionTime = now

        val success = tab?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.d(TAG, "$reason: Attempted to click home tab: $success")
    }

    private fun findNodeByDesc(node: AccessibilityNodeInfo?, desc: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val description = node.contentDescription?.toString()?.trim()
        if (description != null && description.contains(desc, ignoreCase = true)) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeByDesc(child, desc)
            if (result != null) return result
        }

        return null
    }

    private fun logAllDescriptions(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return

        val indent = " ".repeat(depth * 2)
        val desc = node.contentDescription?.toString()
        if (!desc.isNullOrBlank()) {
            Log.d(TAG, "$indent- Desc: \"$desc\", Selected: ${node.isSelected}")
        }

        for (i in 0 until node.childCount) {
            logAllDescriptions(node.getChild(i), depth + 1)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    // === Instagram Functions (keeping these unchanged as requested) ===

    private fun blockInstagramReels(root: AccessibilityNodeInfo) {
        val reelView = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/clips_swipe_refresh_container"
        ).firstOrNull()

        if (reelView != null) {
            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()

            exitTheDoom(feedTab, "Instagram Reels blocked")
        }
    }

    private fun blockInstagramStories(root: AccessibilityNodeInfo) {
        val storyView = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/reel_viewer_root"
        ).firstOrNull()

        if (storyView != null) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()
            exitTheDoom(feedTab, "Instagram Stories blocked")
        }
    }

    private fun blockInstagramExplore(root: AccessibilityNodeInfo) {
        val exploreTab = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/search_tab"
        ).firstOrNull()

        if (exploreTab?.isSelected == true) {
            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()
            exitTheDoom(feedTab, "Instagram Explore blocked")
        }
    }

    private fun exitTheDoom(tab: AccessibilityNodeInfo?, reason: String) {
        val now = System.currentTimeMillis()
        if (now - lastActionTime < debounceMillis) return
        lastActionTime = now

        val success = tab?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.d(TAG, "$reason: Attempted to click feed tab: $success")
    }

    // === Utility Functions ===

    private fun currentMinuteOfDay(): Int {
        val now = System.currentTimeMillis()
        val offset = TimeZone.getDefault().getOffset(now)
        return (((now + offset) / 60000) % 1440).toInt()
    }

    private fun isWithinInterval(start: Int, end: Int, minute: Int): Boolean {
        return if (start <= end) {
            minute in start..end
        } else {
            minute >= start || minute <= end
        }
    }
}
