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
import com.example.v02.ReelsBlockingService.BlockMode
import com.example.v02.ReelsBlockingService.DataStoreManager
import java.util.TimeZone

private const val TAG = "ReelsBlockService"

class ReelsBlockAccessibilityService : AccessibilityService() {

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
                Log.d(
                    TAG,
                    "Settings updated: Instagram Reels blocked = ${latest.instagram.reelsBlocked}"
                )
            }
        }

        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        val nowMin = currentMinuteOfDay()

        if (pkg == "com.instagram.android") {
            val insta = settings.instagram
            if (isWithinInterval(insta.blockedStart, insta.blockedEnd, nowMin)) {
                if (insta.reelsBlocked) {
                    blockInstagramReels(root)
                }
                if (insta.storiesBlocked) {
                    blockInstagramStories(root)
                }
                if (insta.exploreBlocked) {

                    blockInstagramExplore(root)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

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

    private fun blockInstagramReels(root: AccessibilityNodeInfo) {
        val reelView = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/clips_swipe_refresh_container"
        ).firstOrNull()

        if (reelView != null) {
            Log.d(TAG, "Reels detected! Attempting to navigate away")

            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()

            exitTheDoom(feedTab)
        }
    }

    private fun exitTheDoom(targetTab: AccessibilityNodeInfo?) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastActionTime < debounceMillis) {
            return
        }
        lastActionTime = currentTime

        targetTab?.let { tab ->
            val success = tab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Attempted to click feed tab: $success")
        }
    }

    private fun blockInstagramStories(root: AccessibilityNodeInfo) {
        // Try to detect the story container
        val storyView = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/reel_viewer_root" // <- this may vary by version
        ).firstOrNull()

        if (storyView != null) {
            Log.d(TAG, "Stories detected! Attempting to exit...")

            // Press back to exit story
            performGlobalAction(GLOBAL_ACTION_BACK)

            // OR switch to feed tab (if available)
            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()
            exitTheDoom(feedTab)
        }
    }

    private fun blockInstagramExplore(root: AccessibilityNodeInfo) {
        val exploreTab = root.findAccessibilityNodeInfosByViewId(
            "com.instagram.android:id/search_tab"
        ).firstOrNull()

        if (exploreTab != null && exploreTab.isSelected) {
            Log.d(TAG, "Explore tab is active! Attempting to exit...")

            val feedTab = root.findAccessibilityNodeInfosByViewId(
                "com.instagram.android:id/feed_tab"
            ).firstOrNull()

            exitTheDoom(feedTab)
        }
    }


}
