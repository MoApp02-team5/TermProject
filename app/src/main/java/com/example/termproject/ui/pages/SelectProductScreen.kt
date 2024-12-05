package com.example.termproject.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.termproject.R
import com.example.termproject.ui.model.Product
import com.example.termproject.ui.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProductScreen(
    navController: NavController,
    overallViewModel: OverallViewModel // ViewModel 추가
) {
    val productList by overallViewModel.productList.collectAsState() // Firebase에서 가져온 제품 목록
    var selectedProductId by remember { mutableStateOf("") } // 선택된 제품 ID

    LaunchedEffect(Unit) {
        overallViewModel.fetchProducts() // ViewModel에서 데이터를 가져옴
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Select Product")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        val selectedProduct = productList.find { it.id == selectedProductId }
                        if (selectedProduct != null) {
                            // User 객체 생성
                            val user = User(
                                name = selectedProduct.name,
                                id = "", // Firebase에서 자동 생성
                                kcal = selectedProduct.kcal,
                                category = selectedProduct.category,
                                imageurl = selectedProduct.imageurl
                            )

                            // Firebase에 저장
                            overallViewModel.saveSelectedProduct(
                                product = selectedProduct,
                                onSuccess = {
                                    // 추가 작업: date 노드에 kcal 업데이트
                                    overallViewModel.saveKcalToDate(
                                        userId = overallViewModel.currentUserId.value ?: "",
                                        kcal = selectedProduct.kcal,
                                        onSuccess = {
                                            navController.navigate("main") // 저장 성공 시 메인 페이지로 이동
                                        },
                                        onError = { e ->
                                            Log.e("Firebase", "Error updating date node: ${e.message}")
                                        }
                                    )
                                },
                                onError = { e ->
                                    Log.e("Firebase", "Error saving user: ${e.message}")
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "Select")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("camera") {
                    popUpTo("selectProduct") { inclusive = true }
                }
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_camera_alt_24),
                    contentDescription = "Camera"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(productList) { product ->
                    ProductCard(
                        product = product,
                        isSelected = product.id == selectedProductId,
                        onSelect = { selectedProductId = product.id }
                    )
                }
            }
        }
    }
}



@Composable
fun ProductCard(
    product: Product,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽에 이미지 추가
            AsyncImage(
                model = product.imageurl, // 이미지 URL
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(64.dp) // 이미지 크기
                    .padding(end = 16.dp), // 오른쪽 여백
                contentScale = ContentScale.Crop
            )

            // 중앙에 제품 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Category: ${product.category}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Kcal: ${product.kcal}", style = MaterialTheme.typography.bodySmall)
            }

            // 오른쪽에 RadioButton
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}
