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
    private val debounceMillis = 3000L

    override fun onServiceConnected() {
        super.onServiceConnected()
        dataStore = DataStoreManager(this)

        serviceScope.launch {
            dataStore.appSettings.collect { latest ->
                settings = latest
                Log.d(TAG, "Settings updated")
            }
        }

        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return
        val nowMin = currentMinuteOfDay()

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
                blockTabByDescription(root, "Video, tab 2 of 6", "Home, tab 1 of 6")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    // === Instagram Functions ===

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

    // === Facebook Function ===

    private fun blockTabByDescription(root: AccessibilityNodeInfo, blockDesc: String, fallbackDesc: String) {
        val currentTime = System.currentTimeMillis()

        // Debounce: Skip if last action was within debounce period
        if (currentTime - lastActionTime < debounceMillis) return

        val blockedNode = findNodeByDesc(root, blockDesc)
        if (blockedNode != null) {
            Log.d(TAG, "Detected Facebook Video tab")

            val fallbackNode = findNodeByDesc(root, fallbackDesc)
            if (fallbackNode != null) {
                fallbackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Switched to Home tab")
            } else {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d(TAG, "Fallback: Back action")
            }

            // Update block timestamp
            lastActionTime = currentTime
        }
    }


    private fun findNodeByDesc(node: AccessibilityNodeInfo?, desc: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.contentDescription?.toString() == desc) return node

        for (i in 0 until node.childCount) {
            val found = findNodeByDesc(node.getChild(i), desc)
            if (found != null) return found
        }
        return null
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
}
