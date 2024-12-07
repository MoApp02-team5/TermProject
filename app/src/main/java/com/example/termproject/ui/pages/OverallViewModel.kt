package com.example.termproject.ui.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.termproject.ui.model.DateData
import com.google.firebase.database.FirebaseDatabase
import com.example.termproject.ui.model.Product
import com.example.termproject.ui.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class OverallViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("products")
    private val database_user = FirebaseDatabase.getInstance().getReference("user_eat")
    private val databaseDate = FirebaseDatabase.getInstance().getReference("date")

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    private val _userEatList = MutableStateFlow<List<User>>(emptyList())
    val userEatList: StateFlow<List<User>> = _userEatList

    private val _dateDataList = MutableStateFlow<List<DateData>>(emptyList())
    val dateDataList: StateFlow<List<DateData>> = _dateDataList

    init {
        // 로그인 상태 확인 및 사용자 ID 설정
        auth.currentUser?.let { user ->
            _currentUserId.value = user.uid
        }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                auth.currentUser?.let { user ->
                    _currentUserId.value = user.uid
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUserId.value = null
    }


    fun saveProduct(product: Product, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val key = database.push().key // Firebase에서 자동 키 생성
                if (key != null) {
                    val productWithId = product.copy(id = key) // 자동 생성된 ID를 Product에 반영

                    withContext(Dispatchers.IO) {
                        database.child(key).setValue(productWithId).await() // Firebase에 저장
                    }
                    onSuccess() // 저장 성공
                } else {
                    throw Exception("Failed to generate unique ID for product")
                }
            } catch (e: Exception) {
                onError(e) // 저장 실패
            }
        }
    }
    fun fetchProducts() {
        viewModelScope.launch {
            database.get().addOnSuccessListener { snapshot ->
                val products = snapshot.children.mapNotNull { it.getValue<Product>() }
                _productList.value = products
            }.addOnFailureListener { exception ->
                // 실패 처리 (필요 시)
            }
        }
    }

    fun saveSelectedProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 현재 날짜 생성
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val randomId = database_user.push().key ?: UUID.randomUUID().toString()
                val userId = _currentUserId.value ?: throw Exception("User not logged in")

                // Firebase 데이터 구조 생성
                val data = mapOf(
                    "name" to product.name,
                    "category" to product.category,
                    "kcal" to product.kcal,
                    "imageurl" to product.imageurl,
                    "date" to date,
                    "user_id" to userId // 로그인된 사용자 ID 추가
                )

                // Firebase에 저장
                database_user.child(randomId).setValue(data).await()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    fun fetchUserEatData() {
        viewModelScope.launch {
            database_user.get().addOnSuccessListener { snapshot ->
                val users = snapshot.children.mapNotNull { it.getValue<User>() }
                _userEatList.value = users
            }.addOnFailureListener { exception ->
                // 실패 처리 (필요 시 로그 출력)
                Log.e("Firebase", "Error fetching user_eat data: ${exception.message}")
            }
        }
    }

    fun saveKcalToDate(
        userId: String,
        kcal: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 현재 날짜 생성
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val dateRef = FirebaseDatabase.getInstance().getReference("date/$date/$userId")

                // Firebase에서 기존 kcal 가져오기
                val existingData = dateRef.get().await().getValue<Map<String, String>>()
                val existingKcal = existingData?.get("kcal")?.toIntOrNull() ?: 0

                // 새로운 kcal 계산
                val newKcal = existingKcal + kcal.toInt()

                // 업데이트할 데이터
                val updatedData = mapOf(
                    "user_id" to userId,
                    "kcal" to newKcal.toString()
                )

                // Firebase에 업데이트
                dateRef.setValue(updatedData).await()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun fetchKcalDataFromDate() {
        viewModelScope.launch {
            try {
                val dateSnapshot = databaseDate.get().await()

                // 날짜와 데이터를 함께 매핑
                val dateDataList = dateSnapshot.children.flatMap { dateNode ->
                    val dateKey = dateNode.key ?: return@flatMap emptyList()
                    dateNode.children.mapNotNull { snapshot ->
                        snapshot.getValue<DateData>()?.copy(date = dateKey) // 날짜를 포함한 데이터로 매핑
                    }
                }

                _dateDataList.value = dateDataList
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching all date data: ${e.message}")
            }
        }
    }

    fun fetchDataForSpecificDate(selectedDate: String) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: return@launch // 현재 사용자 ID 확인
                val dateSnapshot = databaseDate.child(selectedDate).get().await()
                val filteredDateDataList = dateSnapshot.children.mapNotNull { snapshot ->
                    snapshot.getValue<DateData>()?.takeIf { it.user_id == currentUserId } // user_id 필터링
                }
                _dateDataList.value = filteredDateDataList // 해당 날짜 데이터로 상태 업데이트
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching data for specific date: ${e.message}")
            }
        }
    }

    fun registerUser(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                auth.currentUser?.let { user ->
                    _currentUserId.value = user.uid
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }


}
