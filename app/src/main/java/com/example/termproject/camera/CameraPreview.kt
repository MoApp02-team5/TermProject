package com.example.termproject.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.termproject.ui.pages.CameraViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.nio.ByteBuffer

@Composable
fun CameraPreview(
    navController: NavController,
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val permissionState = remember { mutableStateOf(false) }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState.value = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 요청
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            permissionState.value = true
        }
    }

    if (permissionState.value) {
        // 카메라 권한이 허용된 경우
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    imageCapture = startCameraWithImageCapture(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onError = { Log.e("CameraPreview", "Error: ${it.message}") }
                    )
                    previewView
                }
            )

            Button(
                onClick = {
                    imageCapture?.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val imageData = imageProxyToByteArray(image)
                                viewModel.uploadImageToFirebase(imageData) // ViewModel을 통해 업로드
                                navController.navigate("AddProduct")
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Capture error: ${exception.message}")
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("사진 촬영 및 업로드")
            }
        }
    } else {
        // 권한이 없는 경우 권한 필요 메시지 표시
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("카메라 권한이 필요합니다.")
        }
    }
}

fun imageProxyToByteArray(imageProxy: ImageProxy): ByteArray {
    val plane = imageProxy.planes[0]
    val buffer: ByteBuffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    imageProxy.close() // 이미지 사용 후 반드시 닫아야 함
    return bytes
}


fun startCameraWithImageCapture(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onError: (Exception) -> Unit
): ImageCapture {
    // CameraX의 CameraProvider를 가져옵니다.
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val imageCapture = ImageCapture.Builder().build()

    // 카메라 제공자를 설정합니다.
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // 미리보기 설정
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // 기본 카메라(후면) 선택
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // 기존 바인딩 해제
            cameraProvider.unbindAll()

            // 카메라를 생명주기(LifecycleOwner)에 바인딩하고, 미리보기와 이미지 캡처를 연결
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            // 에러가 발생한 경우 콜백 호출
            onError(exc)
        }
    }, ContextCompat.getMainExecutor(context))

    // 이미지 캡처 객체를 반환
    return imageCapture
}
