package com.example.termproject

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.termproject.ui.pages.AddProductScreen
import com.example.termproject.ui.pages.CalendarScreen
import com.example.termproject.ui.pages.CameraScreen
import com.example.termproject.ui.pages.LoginScreen
import com.example.termproject.ui.pages.MainPageScreen
import com.example.termproject.ui.pages.RegisterScreen
import com.example.termproject.ui.pages.SelectProductScreen
import com.example.termproject.ui.pages.StatsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "selectProduct") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { MainPageScreen(navController) }
        //composable("calendar") { CalendarScreen(navController) }
        composable("stats") { StatsScreen(navController) }
        composable("selectProduct") { SelectProductScreen(navController) }
        composable("addProduct") { AddProductScreen(navController) }
        composable("camera") { CameraScreen(navController) }
    }
}
