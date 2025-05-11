package com.heroapps.memobattle

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heroapps.library.compose.Difficulty

@Composable
fun MemoryGameApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                onStartGame = { difficultyLevel ->
                    navController.navigate("game/$difficultyLevel")
                }
            )
        }
        composable("game/{difficultyLevel}") { backStackEntry ->
            val difficultyLevel = backStackEntry.arguments?.getString("difficultyLevel") ?: Difficulty.Medium.name
            GameScreen(
                difficultyLevel = Difficulty.valueOf(difficultyLevel),
                onBackToMenu = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }
    }
}