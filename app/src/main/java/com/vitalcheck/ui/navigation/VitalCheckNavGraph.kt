package com.vitalcheck.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitalcheck.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vitalcheck.ui.screen.dashboard.DashboardScreen
import com.vitalcheck.ui.screen.foodlog.FoodLogScreen
import com.vitalcheck.ui.screen.history.HistoryScreen
import com.vitalcheck.ui.screen.onboarding.FitbitAuthScreen
import com.vitalcheck.ui.screen.plan.PlanDetailScreen
import com.vitalcheck.ui.screen.settings.SettingsScreen
import com.vitalcheck.ui.theme.DarkSurface

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object FoodLog : Screen("food_log", "Food Log", Icons.Default.Restaurant)
    data object History : Screen("history", "History", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object PlanDetail : Screen("plan_detail", "Plan", null)
    data object Onboarding : Screen("onboarding", "Onboarding", null)
}

private val bottomNavItems = listOf(Screen.Dashboard, Screen.FoodLog, Screen.History, Screen.Settings)

@Composable
fun VitalCheckNavGraph(isAuthenticated: Boolean, targetScreen: StateFlow<String?> = MutableStateFlow(null)) {
    val navController = rememberNavController()
    val startDestination = if (isAuthenticated) Screen.Dashboard.route else Screen.Onboarding.route
    val context = LocalContext.current
    val pendingScreen by targetScreen.collectAsStateWithLifecycle()

    // Handle deep link from notification
    LaunchedEffect(pendingScreen) {
        val screen = pendingScreen ?: return@LaunchedEffect
        navController.navigate(screen) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
        }
        (context as? MainActivity)?.clearTargetScreen()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            } || currentDestination?.route == Screen.PlanDetail.route

            if (showBottomBar) {
                NavigationBar(containerColor = DarkSurface) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.label)
                                }
                            },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                FitbitAuthScreen()
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToPlan = {
                        navController.navigate(Screen.PlanDetail.route)
                    }
                )
            }
            composable(Screen.FoodLog.route) {
                FoodLogScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.PlanDetail.route) {
                PlanDetailScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
