package com.sodapop.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Capture : Screen("capture?voiceMode={voiceMode}") {
        fun createRoute(voiceMode: Boolean = false) = "capture?voiceMode=$voiceMode"
    }
    data object Memory : Screen("memory")
    data object Summary : Screen("summary")
    data object Predictions : Screen("predictions")
    data object PredictionReview : Screen("predictions/{predictionId}") {
        fun createRoute(predictionId: String) = "predictions/$predictionId"
    }
    data object Inspiration : Screen("inspiration")
    data object Dialogue : Screen("dialogue/{thoughtId}?mode={mode}") {
        fun createRoute(thoughtId: String, mode: String = "EXPANSION") =
            "dialogue/$thoughtId?mode=$mode"
    }
    data object Settings : Screen("settings")
}
