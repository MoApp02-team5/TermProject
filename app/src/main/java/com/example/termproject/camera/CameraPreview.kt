package com.example.termproject.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@Composable
fun CameraPreview(
    onImageUploaded: (String) -> Unit, // 업로드된 이미지 URL을 전달하는 콜백
    onError: (Exception) -> Unit, // 에러 발생 시 처리하는 콜백
    modifier: Modifier = Modifier // 기본값 제공
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(), // 전달받은 Modifier 적용
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
                onError = onError
            )
            previewView
        }
    )

    // 사진 촬영 버튼
    Button(onClick = {
        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(file)

                    // 코루틴 스코프를 사용해 비동기 호출
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            uploadImageToFirebase(savedUri, onImageUploaded)
                        } catch (e: Exception) {
                            onError(e)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }) {
        Text("사진 찍기 및 업로드")
    }
}

fun startCameraWithImageCapture(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onError: (Exception) -> Unit
): ImageCapture {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val imageCapture = ImageCapture.Builder().build()

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            onError(exc)
        }
    }, ContextCompat.getMainExecutor(context))

    return imageCapture
}

suspend fun uploadImageToFirebase(
    imageUri: Uri,
    onImageUploaded: (String) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

    try {
        // 이미지 업로드
        val uploadTask = imageRef.putFile(imageUri).await()
        // 업로드된 이미지의 다운로드 URL 가져오기
        val downloadUrl = imageRef.downloadUrl.await().toString()
        onImageUploaded(downloadUrl) // 업로드된 URL 콜백 호출
    } catch (e: Exception) {
        Log.e("FirebaseUpload", "Error uploading image: ${e.message}")
        throw e
    }
}
