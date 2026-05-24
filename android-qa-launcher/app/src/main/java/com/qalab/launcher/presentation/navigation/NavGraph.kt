package com.qalab.launcher.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.qalab.launcher.presentation.ui.dashboard.DashboardScreen
import com.qalab.launcher.presentation.ui.monitoring.MonitoringScreen
import com.qalab.launcher.presentation.ui.devicelab.DeviceLabScreen
import com.qalab.launcher.presentation.ui.sandbox.SandboxScreen
import com.qalab.launcher.presentation.ui.plugin.PluginScreen
import com.qalab.launcher.presentation.ui.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: String) {
    data object Dashboard : Screen("dashboard", "Dashboard", "dashboard")
    data object Monitoring : Screen("monitoring", "Monitoring", "monitor")
    data object DeviceLab : Screen("device_lab", "Device Lab", "devices")
    data object Sandbox : Screen("sandbox", "Sandbox", "sandbox")
    data object Plugins : Screen("plugins", "Plugins", "extension")
    data object Settings : Screen("settings", "Settings", "settings")
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Monitoring,
    Screen.DeviceLab,
    Screen.Sandbox,
    Screen.Plugins
)

@Composable
fun QALabNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Monitoring.route) {
            MonitoringScreen()
        }
        composable(Screen.DeviceLab.route) {
            DeviceLabScreen()
        }
        composable(Screen.Sandbox.route) {
            SandboxScreen()
        }
        composable(Screen.Plugins.route) {
            PluginScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
