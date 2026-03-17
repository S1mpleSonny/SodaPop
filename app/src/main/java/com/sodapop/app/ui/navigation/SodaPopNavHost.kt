package com.sodapop.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sodapop.app.ui.capture.CaptureScreen
import com.sodapop.app.ui.dialogue.DialogueScreen
import com.sodapop.app.ui.home.HomeScreen
import com.sodapop.app.ui.inspiration.InspirationScreen
import com.sodapop.app.ui.memory.MemoryScreen
import com.sodapop.app.ui.prediction.PredictionScreen
import com.sodapop.app.ui.settings.SettingsScreen
import com.sodapop.app.ui.summary.SummaryScreen

@Composable
fun SodaPopNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCapture = {
                    navController.navigate(Screen.Capture.createRoute())
                },
                onNavigateToDialogue = { thoughtId ->
                    navController.navigate(Screen.Dialogue.createRoute(thoughtId))
                },
                onNavigateToInspiration = {
                    navController.navigate(Screen.Inspiration.route)
                }
            )
        }

        composable(
            route = Screen.Capture.route,
            arguments = listOf(
                navArgument("voiceMode") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            CaptureScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(Screen.Memory.route) {
            MemoryScreen(
                onNavigateToDialogue = { thoughtId ->
                    navController.navigate(Screen.Dialogue.createRoute(thoughtId))
                }
            )
        }

        composable(Screen.Summary.route) {
            SummaryScreen()
        }

        composable(Screen.Predictions.route) {
            PredictionScreen()
        }

        composable(Screen.Inspiration.route) {
            InspirationScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Dialogue.route,
            arguments = listOf(
                navArgument("thoughtId") { type = NavType.StringType },
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "EXPANSION"
                }
            )
        ) {
            DialogueScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
