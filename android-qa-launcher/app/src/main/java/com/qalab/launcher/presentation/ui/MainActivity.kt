package com.qalab.launcher.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qalab.launcher.presentation.navigation.QALabNavGraph
import com.qalab.launcher.presentation.navigation.Screen
import com.qalab.launcher.presentation.navigation.bottomNavScreens
import com.qalab.launcher.presentation.theme.QALabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QALabTheme(darkTheme = true) {
                QALabApp()
            }
        }
    }
}

@Composable
fun QALabApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { QALabBottomBar(navController) }
    ) { paddingValues ->
        QALabNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun QALabBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = getIconForScreen(screen),
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun getIconForScreen(screen: Screen): ImageVector {
    return when (screen) {
        Screen.Dashboard -> Icons.Default.Dashboard
        Screen.Monitoring -> Icons.Default.Monitor
        Screen.DeviceLab -> Icons.Default.Devices
        Screen.Sandbox -> Icons.Default.Shield
        Screen.Plugins -> Icons.Default.Extension
        else -> Icons.Default.Dashboard
    }
}
