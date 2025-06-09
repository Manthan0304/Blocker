package com.example.v02.timelimit.Screens

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.v02.timelimit.AppLimits
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppLimitsScreen() {
    val context = LocalContext.current
    var limitedApps by remember { mutableStateOf<List<LimitedAppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        limitedApps = loadLimitedApps(context)
        isLoading = false
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading app limits...")
        }
    } else if (limitedApps.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No app limits set",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Go to Apps tab to set limits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(limitedApps) { limitedApp ->
                LimitedAppItem(
                    limitedApp = limitedApp,
                    onRemoveLimit = {
                        AppLimits.removeLimit(limitedApp.packageName)
                        AppLimits.saveLimits()
                        scope.launch {
                            limitedApps = loadLimitedApps(context)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LimitedAppItem(
    limitedApp: LimitedAppItem,
    onRemoveLimit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberDrawablePainter(limitedApp.icon),
                contentDescription = limitedApp.appName,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = limitedApp.appName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Limit: ${limitedApp.limitMinutes} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemoveLimit) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove limit",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class LimitedAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val limitMinutes: Int
)

private suspend fun loadLimitedApps(context: Context): List<LimitedAppItem> =
    withContext(Dispatchers.IO) {
        try {
            val launcherApps =
                context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val packageManager = context.packageManager

            val limits = AppLimits.getAllLimits()

            limits.mapNotNull { (packageName, limitMinutes) ->
                try {
                    val applicationInfo = launcherApps.getApplicationInfo(
                        packageName, 0, Process.myUserHandle()
                    )
                    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                    val icon = packageManager.getApplicationIcon(applicationInfo)

                    LimitedAppItem(
                        packageName = packageName,
                        appName = appName,
                        icon = icon,
                        limitMinutes = limitMinutes
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
