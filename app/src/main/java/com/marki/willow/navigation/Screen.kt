package com.marki.willow.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SleepLog : Screen("sleep_log")
    object SleepHistory : Screen("sleep_history")
    object ExerciseLog : Screen("exercise_log")
    object ExerciseHistory : Screen("exercise_history")
    object Conflicts : Screen("conflicts")
    object Settings : Screen("settings")
}