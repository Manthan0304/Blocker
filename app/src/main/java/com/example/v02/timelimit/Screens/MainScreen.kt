package com.example.v02.timelimit.Screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("Apps", Icons.Default.Apps),
        TabItem("Limits", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Time Limit") }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                0 -> navController.navigate("apps") {
                                    popUpTo("apps") { inclusive = true }
                                }
                                1 -> navController.navigate("limits") {
                                    popUpTo("limits") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "apps",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("apps") {
                selectedTab = 0
                AppUsageScreen(navController = navController)
            }
            composable("limits") {
                selectedTab = 1
                AppLimitsScreen()
            }
            composable("set_limit/{packageName}/{appName}") { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                val appName = backStackEntry.arguments?.getString("appName") ?: ""
                SetLimitScreen(
                    packageName = packageName,
                    appName = appName,
                    navController = navController
                )
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector
)
