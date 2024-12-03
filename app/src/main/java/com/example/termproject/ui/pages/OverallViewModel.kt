package com.example.termproject.ui.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.example.termproject.ui.model.Product
import com.example.termproject.ui.model.User
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

    private val database = FirebaseDatabase.getInstance().getReference("products")
    private val database_user = FirebaseDatabase.getInstance().getReference("user_eat")

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    private val _userEatList = MutableStateFlow<List<User>>(emptyList())
    val userEatList: StateFlow<List<User>> = _userEatList

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

                // Firebase 데이터 구조 생성
                val data = mapOf(
                    "name" to product.name,
                    "category" to product.category,
                    "kcal" to product.kcal,
                    "imageurl" to product.imageurl,
                    "date" to date
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
}
