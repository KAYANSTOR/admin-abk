package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.AppMainScreen

@Composable
fun AppNavigation(isLoggedIn: Boolean, onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "login") {
        composable("login") { 
            LoginScreen(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            ) 
        }
        composable("main") { 
            AppMainScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            ) 
        }
    }
}
