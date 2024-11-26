package com.example.termproject.ui.pages

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.termproject.R
import com.example.termproject.camera.CameraPreview
import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.padding

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val permissionState = remember { mutableStateOf(false) }

    // 권한 요청 런처 설정
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addProduct") }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_camera_24),
                    contentDescription = ""
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center, // FAB를 중앙에 정렬
    ) { innerPadding ->
        // 권한 확인
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                permissionState.value = true
            }
        }

        // 카메라 화면 또는 권한 요청 메시지
        if (permissionState.value) {
            // CameraPreview 호출
            CameraPreview(
                onImageUploaded = { imageUrl ->
                    // 업로드된 이미지 URL을 NavController로 전달
                    navController.navigate("addProduct?imageUrl=$imageUrl")
                },
                onError = { exception ->
                    // 에러 발생 시 로그 출력
                    Log.e("CameraScreen", "Error uploading image: ${exception.message}")
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Text(
                text = "카메라 권한이 필요합니다.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // innerPadding을 적용
                textAlign = TextAlign.Center
            )
        }
    }

}

