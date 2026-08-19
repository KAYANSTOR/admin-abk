import re

filepath = "/app/applet/app/src/main/java/com/example/ui/navigation/AppNavigation.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

new_content = """package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.AppMainScreen

@Composable
fun AppNavigation(isLoggedIn: Boolean, startRoute: String? = null, onLoginSuccess: () -> Unit) {
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
                initialRoute = startRoute,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            ) 
        }
    }
}
"""

with open(filepath, "w", encoding="utf-8") as f:
    f.write(new_content)

