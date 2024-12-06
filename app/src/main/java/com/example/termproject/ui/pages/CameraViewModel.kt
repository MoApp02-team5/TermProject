package com.example.termproject.ui.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class CameraViewModel : ViewModel() {

    private val _uploadState = MutableStateFlow<Result<String>?>(null)
    val uploadState: StateFlow<Result<String>?> = _uploadState

    private val _analysisState = MutableStateFlow<Result<String>?>(null)
    val analysisState: StateFlow<Result<String>?> = _analysisState

    // Firebase Storage에 이미지 업로드
    fun uploadImageToFirebase(imageData: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

                imageRef.putBytes(imageData).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                withContext(Dispatchers.Main) {
                    _uploadState.value = Result.success(downloadUrl)
                    Log.d("FirebaseUpload", "이미지 업로드 성공: $downloadUrl")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uploadState.value = Result.failure(e)
                    Log.e("FirebaseUpload", "이미지 업로드 실패: ${e.message}")
                }
            }
        }
    }

    // OpenAI API 호출 (공통 함수)
    private suspend fun makeApiCall(
        payload: JSONObject,
        apiKey: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val body = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val result = JSONObject(responseBody).getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").getString("content").trim()
                    Log.d("OpenAI", "API 호출 성공: $result")
                    Result.success(result)
                } else {
                    val errorMessage = "API 호출 실패: HTTP ${response.code} - ${response.message}"
                    Log.e("OpenAI", errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("OpenAI", "API 호출 중 예외 발생: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // OpenAI API: 이미지 분석
    fun analyzeImageWithChatGPT(imageUrl: String, apiKey: String) {
        viewModelScope.launch {
            _analysisState.value = Result.failure(Exception("Loading"))
            val payload = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put(
                                    "text",
                                    "Provide images to analyze. Analyze the images and return the calories of the products shown on the screen as numbers. If the analysis is ambiguous or the product is not recognized, return 0. Return only the number."
                                )
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", imageUrl)
                                })
                            })
                        })
                    })
                })
                put("max_tokens", 300)
            }

            _analysisState.value = makeApiCall(payload, apiKey)
        }
    }

    // OpenAI API: 제품명 분석
    fun analyzeProductWithChatGPT(productName: String, apiKey: String) {
        viewModelScope.launch {
            _analysisState.value = Result.failure(Exception("Loading"))
            val payload = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Analyze the product name: $productName. Return only the calories as a number. If the product is ambiguous or unknown, return 0. Only return the numbers.")
                    })
                })
                put("max_tokens", 300)
            }

            _analysisState.value = makeApiCall(payload, apiKey)
        }
    }
}
