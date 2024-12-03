package com.example.termproject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.termproject.ui.pages.AddProductScreen
import com.example.termproject.ui.pages.CalendarScreen
import com.example.termproject.ui.pages.CameraScreen
import com.example.termproject.ui.pages.CameraViewModel
import com.example.termproject.ui.pages.LoginScreen
import com.example.termproject.ui.pages.MainPageScreen
import com.example.termproject.ui.pages.OverallViewModel
import com.example.termproject.ui.pages.RegisterScreen
import com.example.termproject.ui.pages.SelectProductScreen
import com.example.termproject.ui.pages.StatsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModelcamera: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val viewModeloverall: OverallViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { MainPageScreen(
            navController,
            viewModeloverall
        ) }
        composable("calendar") { CalendarScreen(navController) }
        composable("stats") { StatsScreen(navController) }
        composable("selectProduct") {
            SelectProductScreen(
                navController,
                viewModeloverall
            ) }
        composable("addProduct") {
            AddProductScreen(
                navController,
                viewModeloverall,
                viewModelcamera
            )
        }
        composable("camera") {
            CameraScreen(
                navController,
                viewModelcamera
            ) }
    }
}
