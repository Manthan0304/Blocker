package com.example.v02.timelimit.Screens

import com.example.v02.timelimit.AppLimits
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Process
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun AppUsageScreen(navController: NavController) {
    val context = LocalContext.current
    var appStats by remember { mutableStateOf<List<AppStatsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            loadAppUsageStats(context) { stats ->
                appStats = stats
                isLoading = false
            }
            delay(5000) // Refresh every 5 seconds
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading app usage statistics...")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appStats) { appStat ->
                AppUsageItem(
                    appStat = appStat,
                    onClick = {
                        if (appStat.packageName != context.packageName) {
                            val encodedPackageName = Uri.encode(appStat.packageName)
                            val encodedAppName = Uri.encode(appStat.appName)
                            navController.navigate("set_limit/$encodedPackageName/$encodedAppName")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppUsageItem(
    appStat: AppStatsItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isCurrentApp = appStat.packageName == context.packageName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentApp) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberDrawablePainter(appStat.icon),
                contentDescription = appStat.appName,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appStat.appName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Today: ${formatTime(appStat.usageTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isCurrentApp) {
                    Text(
                        text = "Cannot set limit for this app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val limit = AppLimits.getLimit(appStat.packageName)
                    if (limit > 0) {
                        val usageMinutes = appStat.usageTime / (1000 * 60)
                        val remainingMinutes = limit - usageMinutes

                        if (remainingMinutes > 0) {
                            Text(
                                text = "Remaining: ${remainingMinutes}m (limit: ${limit}m)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "⚠️ LIMIT EXCEEDED! (${limit}m limit)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Tap to set limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

data class AppStatsItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val usageTime: Long
)

private suspend fun loadAppUsageStats(
    context: Context,
    onResult: (List<AppStatsItem>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val packageManager = context.packageManager

            val appUsageStats = AppLimits.getUsageStats(usageStatsManager)

            val filteredStats = appUsageStats
                .filter { it.totalTimeInForeground > 5 * 1000 }
                .mapNotNull { stats ->
                    try {
                        val applicationInfo = launcherApps.getApplicationInfo(
                            stats.packageName, 0, Process.myUserHandle()
                        )
                        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                        val icon = packageManager.getApplicationIcon(applicationInfo)

                        AppStatsItem(
                            packageName = stats.packageName,
                            appName = appName,
                            icon = icon,
                            usageTime = stats.totalTimeInForeground
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedByDescending { it.usageTime }

            withContext(Dispatchers.Main) {
                onResult(filteredStats)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val hours = timeInMillis / (1000 * 60 * 60)
    val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
