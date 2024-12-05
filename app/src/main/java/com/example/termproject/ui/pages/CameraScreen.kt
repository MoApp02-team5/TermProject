package com.example.termproject.ui.pages

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.termproject.camera.CameraPreview

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        CameraPreview(
            navController = navController, // NavController 전달
            viewModel = viewModel, // ViewModel을 전달
            modifier = Modifier.padding(innerPadding)
        )
    }
}