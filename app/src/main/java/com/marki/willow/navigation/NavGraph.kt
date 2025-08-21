package com.marki.willow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marki.willow.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.SleepLog.route) {
            SleepLogScreen(navController = navController)
        }
        
        composable(Screen.SleepHistory.route) {
            SleepHistoryScreen(navController = navController)
        }
        
        composable(Screen.ExerciseLog.route) {
            ExerciseLogScreen(navController = navController)
        }
        
        composable(Screen.ExerciseHistory.route) {
            ExerciseHistoryScreen(navController = navController)
        }
        
        composable(Screen.Conflicts.route) {
            ConflictsScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}