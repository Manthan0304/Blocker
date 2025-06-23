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
            }
        }
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
                val fb = settings.facebook
                if (isWithinInterval(fb.blockedStart, fb.blockedEnd, nowMin)) {
                    if (fb.reelsBlocked) blockFacebookReels(root)
                    if (fb.marketplaceBlocked) blockFacebookMarketplace(root)
                }
            }
        }
    }

    private fun blockFacebookReels(root: AccessibilityNodeInfo) {
        val now = System.currentTimeMillis()
        if (now - lastActionTime < debounceMillis) return

        val selectedTab = findSelectedTab(root)
        val selectedDesc = selectedTab?.contentDescription?.toString()?.lowercase()?.trim()

        if (selectedDesc != null) {
            Log.d("FB_BLOCKER", "Selected Tab: $selectedDesc")

            if (selectedDesc.startsWith("video")) {
                lastActionTime = now
                val homeNode = findNodeByDesc(root, "Home")
                if (homeNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                    Log.d("FB_BLOCKER", "Reels blocked → Home tab clicked")
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d("FB_BLOCKER", "Reels blocked → Fallback BACK used")
                }
            }
        }
    }

    private fun blockFacebookMarketplace(root: AccessibilityNodeInfo) {
        val selectedTab = findSelectedTab(root)
        val selectedDesc = selectedTab?.contentDescription?.toString()?.lowercase()?.trim()

        if (selectedDesc != null) {
            Log.d("FB_BLOCKER", "Selected Tab: $selectedDesc")

            if (selectedDesc.contains("marketplace")) {
                lastActionTime = System.currentTimeMillis()
                val homeNode = findNodeByDesc(root, "Home")
                if (homeNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                    Log.d("FB_BLOCKER", "Marketplace blocked → Home tab clicked")
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d("FB_BLOCKER", "Marketplace blocked → Fallback BACK used")
                }
            }
        }
    }

    private fun findSelectedTab(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isSelected && node.contentDescription != null) return node

        for (i in 0 until node.childCount) {
            val result = findSelectedTab(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun blockInstagramReels(root: AccessibilityNodeInfo) {
        val reelView = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_swipe_refresh_container").firstOrNull()
        if (reelView != null) {
            val feedTab = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab").firstOrNull()
            Log.d("INSTA_BLOCKER", "Instagram Reels detected, redirecting to Feed")
            exitTheDoom(feedTab, "Instagram Reels blocked")
        }
    }

    private fun blockInstagramStories(root: AccessibilityNodeInfo) {
        val storyView = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_root").firstOrNull()
        if (storyView != null) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            val feedTab = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab").firstOrNull()
            Log.d("INSTA_BLOCKER", "Instagram Stories detected, redirecting to Feed")
            exitTheDoom(feedTab, "Instagram Stories blocked")
        }
    }

    private fun blockInstagramExplore(root: AccessibilityNodeInfo) {
        val exploreTab = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/search_tab").firstOrNull()
        if (exploreTab?.isSelected == true) {
            val feedTab = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/feed_tab").firstOrNull()
            Log.d("INSTA_BLOCKER", "Instagram Explore detected, redirecting to Feed")
            exitTheDoom(feedTab, "Instagram Explore blocked")
        }
    }

    private fun exitTheDoom(tab: AccessibilityNodeInfo?, reason: String) {
        val now = System.currentTimeMillis()
        if (now - lastActionTime < debounceMillis) return
        lastActionTime = now
        val success = tab?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.d("INSTA_BLOCKER", "$reason: Clicked feed tab: $success")
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        serviceScope.cancel()
    }
}
