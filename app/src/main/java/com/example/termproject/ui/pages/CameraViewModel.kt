package com.example.termproject.ui.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CameraViewModel : ViewModel() {

    private val _uploadState = MutableStateFlow<Result<String>?>(null)
    val uploadState: StateFlow<Result<String>?> = _uploadState

    fun uploadImageToFirebase(imageData: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

                imageRef.putBytes(imageData).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                withContext(Dispatchers.Main) {
                    _uploadState.value = Result.success(downloadUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uploadState.value = Result.failure(e)
                }
            }
        }
    }
}
