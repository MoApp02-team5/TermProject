package com.example.termproject.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.termproject.ui.model.Product


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    overallViewModel: OverallViewModel,
    viewModel: CameraViewModel
) {
    val uploadState by viewModel.uploadState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState() // OpenAI API 결과 상태
    val apiKey = "" // OpenAI API 키

    var name = remember { mutableStateOf("") }
    var category = remember { mutableStateOf("") }
    var kcal = remember { mutableStateOf("") }
    val imageUrl = uploadState?.getOrNull() ?: ""

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                actions = {
                    // 사진 버튼에 OpenAI API 호출 연결
                    Button(onClick = {
                        analyzeUploadedImage(viewModel, apiKey) { result ->
                            kcal.value = result // 분석 결과를 kcal에 반영
                        }
                    }) {
                        Text("사진")
                    }
                    Button(onClick = {
                        val productName = name.value // 사용자가 입력한 제품명
                        viewModel.analyzeProductWithChatGPT(productName, apiKey)
                    }) {
                        Text("제품명")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Firebase 업로드 상태 표시
            UploadStateContent(uploadState = uploadState)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category.value,
                onValueChange = { category.value = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = kcal.value,
                onValueChange = { input ->
                    // 숫자만 허용
                    if (input.all { it.isDigit() }) {
                        kcal.value = input
                    }
                },
                label = { Text("Kcal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 분석 결과 표시
            AnalysisStateContent(analysisState = analysisState)

            Button(onClick = {
                val product = Product(
                    name = name.value,
                    category = category.value,
                    id = "", // ID는 Firebase에서 자동 생성됨
                    kcal = kcal.value,
                    imageurl = imageUrl
                )

                overallViewModel.saveProduct(
                    product = product,
                    onSuccess = { /* 저장 성공 시 처리 */ },
                    onError = { e -> /* 저장 실패 시 처리 */ }
                )

                navController.navigate("selectProduct") {
                    popUpTo("addProduct") {inclusive = true}
                }
            }) {
                Text("Save to Firebase")
            }
        }
    }
}


@Composable
fun UploadStateContent(uploadState: Result<String>?) {
    uploadState?.let { result ->
        result.onSuccess { url ->
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // 이미지 표시
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Uploaded Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }.onFailure { exception ->
            Text(
                text = "업로드 실패: ${exception.message}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    } ?: Text(
        text = "이미지 업로드 상태를 확인 중...",
        style = MaterialTheme.typography.bodyLarge
    )
}


fun analyzeUploadedImage(viewModel: CameraViewModel, apiKey: String, updateKcal: (String) -> Unit) {
    val imageUrl = viewModel.uploadState.value?.getOrNull()
    if (imageUrl != null) {
        viewModel.analyzeImageWithChatGPT(imageUrl, apiKey)

        // ViewModel의 StateFlow를 observe 하고 업데이트
        viewModel.analysisState.value?.onSuccess { analysisResult ->
            val kcalValue = analysisResult.toIntOrNull()?.toString() ?: "" // Int를 String으로 변환
            updateKcal(kcalValue) // 분석 결과를 업데이트
        }
    }
}

@Composable
fun AnalysisStateContent(analysisState: Result<String>?) {
    analysisState?.let { result ->
        result.onSuccess { analysisResult ->
            Text(
                text = "분석 결과: $analysisResult",
                style = MaterialTheme.typography.bodyLarge
            )
        }.onFailure { exception ->
            Text(
                text = "분석 실패: ${exception.message}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    } ?: Text(
        text = "분석 대기 중...",
        style = MaterialTheme.typography.bodyLarge
    )
}
