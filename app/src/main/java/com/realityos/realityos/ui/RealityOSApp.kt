package com.realityos.realityos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.realityos.realityos.ui.elite.EliteLockedScreen
import com.realityos.realityos.ui.history.HistoryScreen
import com.realityos.realityos.ui.home.HomeScreen
import com.realityos.realityos.ui.modes.ModesScreen
import com.realityos.realityos.ui.onboarding.OnboardingScreen
import com.realityos.realityos.ui.rules.RulesScreen
import com.realityos.realityos.ui.settings.SettingsScreen


@Composable
fun RealityOSApp(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(onOnboardingComplete = {
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("home") { HomeScreen(navController = navController) }
        composable("rules") { RulesScreen(navController = navController) }
        composable("history") { HistoryScreen(navController = navController) }
        composable("modes") { ModesScreen(navController = navController) }
        composable("settings") { SettingsScreen(navController = navController) }
        composable("elite") { EliteLockedScreen(navController = navController) }
    }
}
