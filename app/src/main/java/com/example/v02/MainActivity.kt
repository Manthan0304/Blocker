package com.example.v02

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.v02.timelimit.AppMonitoringService
import com.example.v02.timelimit.Screens.MainScreen
import com.example.v02.timelimit.Screens.PermissionScreen
import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.v02.ReelsBlockingService.MainViewModel
import kotlinx.coroutines.delay
import android.view.accessibility.AccessibilityManager
import com.example.v02.ReelsBlockingService.BlockMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = MainViewModel(application = application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(viewModel)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Check permissions again when app resumes
        lifecycleScope.launch {
            delay(500) // Small delay to ensure settings are applied
            if (hasRequiredPermissions()) {
                AppMonitoringService.start(this@MainActivity)
            }
        }
    }
    fun hasRequiredPermissions(): Boolean {
        return hasUsageStatsPermission() && isAccessibilityServiceEnabled()
    }

    fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

            val expectedServiceName = ComponentName(this, "com.example.v02.timelimit.AppBlockerAccessibilityService")

            // Method 1: Check using enabled services list
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )

            Log.d("MainActivity", "Checking ${enabledServices.size} enabled accessibility services")

            for (serviceInfo in enabledServices) {
                val serviceId = serviceInfo.resolveInfo.serviceInfo
                val serviceName = "${serviceId.packageName}/${serviceId.name}"
                Log.d("MainActivity", "Found enabled service: $serviceName")

                if (serviceId.packageName == packageName &&
                    serviceId.name == "com.example.v02.timelimit.AppBlockerAccessibilityService") {
                    Log.d("MainActivity", "✅ Found our accessibility service via method 1")
                    return true
                }
            }

            // Method 2: Check using Settings.Secure (more reliable)
            val enabledServicesSetting = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            Log.d("MainActivity", "Enabled services setting: $enabledServicesSetting")

            if (enabledServicesSetting.isNullOrEmpty()) {
                Log.d("MainActivity", "No enabled services found in settings")
                return false
            }

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                Log.d("MainActivity", "Checking service: $componentNameString")

                val componentName = ComponentName.unflattenFromString(componentNameString)
                if (componentName != null) {
                    if (componentName.packageName == packageName &&
                        componentName.className == "com.example.v02.timelimit.AppBlockerAccessibilityService") {
                        Log.d("MainActivity", "✅ Found our accessibility service via method 2")
                        return true
                    }

                    // Also check with just the class name (sometimes the full path is used)
                    if (componentName.packageName == packageName &&
                        componentName.className.endsWith("AppBlockerAccessibilityService")) {
                        Log.d("MainActivity", "✅ Found our accessibility service via method 2 (partial match)")
                        return true
                    }
                }
            }

            Log.d("MainActivity", "❌ Accessibility service not found")
            false
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking accessibility service: ${e.message}")
            false
        }
    }

    fun getPermissionStatus(): Pair<Boolean, Boolean> {
        return Pair(hasUsageStatsPermission(), isAccessibilityServiceEnabled())
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val isReelsBlockingEnabled by viewModel.isReelsBlockingEnabled.collectAsState(initial = false)
    val isStoriesBlockingEnabled by viewModel.isStoriesBlockingEnabled.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Instagram Blocker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Reels Block Card
        BlockCard(
            title = "Block Instagram Reels",
            description = "Automatically navigates away from Reels to keep you focused.",
            checked = isReelsBlockingEnabled,
            onToggle = { viewModel.setReelsBlockingEnabled(it) }
        )

        // Stories Block Card
        BlockCard(
            title = "Block Instagram Stories",
            description = "Automatically exits Stories view to avoid distractions.",
            checked = isStoriesBlockingEnabled,
            onToggle = { viewModel.setStoriesBlockingEnabled(it) }
        )

        val isExploreBlockingEnabled by viewModel.isExploreBlockingEnabled.collectAsState(initial = false)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Block Instagram Explore",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Automatically navigates away from Explore to keep you focused.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Enable Explore Blocking",
                        fontSize = 16.sp
                    )

                    Switch(
                        checked = isExploreBlockingEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setExploreBlockingEnabled(enabled)
                        }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }) {
            Text("Enable Accessibility Service")
        }
    }
}

@Composable
fun BlockCard(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable", fontSize = 16.sp)
                Switch(checked = checked, onCheckedChange = onToggle)
            }
        }
    }
}


@Composable
fun AppLimitApp() {
    val context = LocalContext.current
    var hasUsagePermission by remember { mutableStateOf(false) }
    var hasAccessibilityPermission by remember { mutableStateOf(false) }
    var isCheckingPermission by remember { mutableStateOf(true) }

    val hasAllPermissions = hasUsagePermission && hasAccessibilityPermission

    // Check permissions periodically when on permission screen
    LaunchedEffect(hasAllPermissions) {
        while (!hasAllPermissions) {
            val activity = context as? MainActivity
            if (activity != null) {
                Log.d("AppLimitApp", "Checking permissions...")
                val (usage, accessibility) = activity.getPermissionStatus()
                Log.d("AppLimitApp", "Usage permission: $usage, Accessibility permission: $accessibility")
                hasUsagePermission = usage
                hasAccessibilityPermission = accessibility
            }
            isCheckingPermission = false

            if (hasAllPermissions) {
                Log.d("AppLimitApp", "All permissions granted! Starting monitoring service...")
                // Start monitoring service when permissions are granted
                AppMonitoringService.start(context)
                break
            }

            delay(2000) // Check every 2 seconds (increased from 1 second)
        }
    }

    if (hasAllPermissions) {
        MainScreen()
    } else {
        PermissionScreen(
            isCheckingPermission = isCheckingPermission,
            hasUsagePermission = hasUsagePermission,
            hasAccessibilityPermission = hasAccessibilityPermission,
            onRequestUsagePermission = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            },
            onRequestAccessibilityPermission = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )
    }
}
