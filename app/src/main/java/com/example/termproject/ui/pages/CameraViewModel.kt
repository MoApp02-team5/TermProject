package com.example.termproject.ui.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream

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
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uploadState.value = Result.failure(e)
                }
            }
        }
    }

    // OpenAI API 호출
    fun analyzeImageWithChatGPT(imageUrl: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _analysisState.value = Result.failure(Exception("Loading")) // 로딩 시작 상태를 임시로 표현
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("model", "gpt-4o-mini")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("type", "text")
                                    put(
                                        "text",
                                        "분석할 이미지를 제공합니다. 이미지를 분석하여 화면에 보이는 제품의 칼로리를 숫자로 반환하세요. 분석이 애매하거나 제품을 인식할 수 없는 경우, 0을 반환하세요. 오직 제품명과 숫자만 반환하세요."
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

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val result = JSONObject(responseBody).getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").getString("content")
                    withContext(Dispatchers.Main) {
                        _analysisState.value = Result.success(result) // 성공 상태와 결과 저장
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorMessage = "API 호출 실패: HTTP ${response.code} - ${response.message}"
                        _analysisState.value = Result.failure(Exception(errorMessage))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _analysisState.value = Result.failure(e) // 실패 상태 저장
                }
            }
        }
    }

    fun analyzeProductWithChatGPT(productName: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _analysisState.value = Result.failure(Exception("Loading")) // 로딩 상태 설정
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("model", "gpt-4o-mini")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Analyze the product name: $productName. Return only the calories as a number. If the product is ambiguous or unknown, return 0. 오직 숫자만 반환하세요.")
                        })
                    })
                    put("max_tokens", 300)
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val result = JSONObject(responseBody).getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").getString("content")
                    withContext(Dispatchers.Main) {
                        _analysisState.value = Result.success(result.trim()) // 성공 상태와 결과 저장
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorMessage = "API 호출 실패: HTTP ${response.code} - ${response.message}"
                        _analysisState.value = Result.failure(Exception(errorMessage))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _analysisState.value = Result.failure(e) // 실패 상태 저장
                }
            }
        }
    }



}