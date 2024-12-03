package com.example.termproject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.termproject.ui.product.AddProductScreen
import com.example.termproject.ui.calendar.CalendarScreen
import com.example.termproject.ui.camera.CameraScreen
import com.example.termproject.ui.login.LoginScreen
import com.example.termproject.ui.home.MainPageScreen
import com.example.termproject.ui.register.RegisterScreen
import com.example.termproject.ui.product.SelectProductScreen
import com.example.termproject.ui.stats.StatsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { MainPageScreen(navController) }
        composable("calendar") { CalendarScreen(navController) }
        composable("stats") { StatsScreen(navController) }
        composable("selectProduct") { SelectProductScreen(navController) }
        composable("addProduct") { AddProductScreen(navController) }
        composable("camera") { CameraScreen(navController) }
    }
}
