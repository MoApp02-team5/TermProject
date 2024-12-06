package com.example.termproject.ui.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.termproject.R
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
    val isLoading = analysisState?.exceptionOrNull()?.message == "Loading"


    var name = remember { mutableStateOf("") }
    var category = remember { mutableStateOf("") }
    var kcal = remember { mutableStateOf("0") }
    val imageUrl = uploadState?.getOrNull() ?: ""

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                actions = {
                    // 버튼을 Row로 감싸고 패딩 추가
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp), // 버튼 사이의 간격 추가
                        modifier = Modifier.padding(end = 8.dp) // Row의 오른쪽 여백
                    ) {
                        // AnalyzeImageButton
                        AnalyzeImageButton(
                            isLoading = isLoading,
                            onClick = {
                                analyzeUploadedImage(viewModel, apiKey) { result ->
                                    Log.e("AnalyzeResult", "분석 결과: $result") // 로그에 result 출력
                                    kcal.value = result // 분석 결과를 kcal에 반영
                                }
                            }
                        )

                        // AnalyzeNameButton
                        AnalyzeNameButton(
                            isLoading = isLoading ?: false,
                            name = name.value,
                            onClick = {
                                viewModel.analyzeProductWithChatGPT(name.value, apiKey)
                            }
                        )
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
                label = { Text("제품명") },
                modifier = Modifier.fillMaxWidth()
            )

            CategoryDropdown(
                category = category.value,
                onCategorySelected = { selectedCategory ->
                    category.value = selectedCategory
                }
            )

            OutlinedTextField(
                value = kcal.value,
                onValueChange = { input ->
                    // 숫자만 허용
                    if (input.all { it.isDigit() }) {
                        kcal.value = input
                    }
                },
                label = { Text("칼로리") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 분석 결과 표시
            AnalysisStateContent(analysisState = analysisState)

            Button(
                onClick = {
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
                        popUpTo("addProduct") { inclusive = true }
                    }
                },
                enabled = name.value.isNotEmpty() && category.value.isNotEmpty() && kcal.value.isNotEmpty(), // 입력 여부에 따라 활성화
                modifier = Modifier
                    .fillMaxWidth() // 화면 너비에 맞게 확장
                    .padding(horizontal = 16.dp) // 양쪽 여백 설정
                    .height(56.dp), // 버튼 높이 설정
                shape = MaterialTheme.shapes.small.copy(CornerSize(0.dp)) // 사각형 모양
            ) {
                Text("제품 추가하기")
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
            Log.e("AnalyzeResult", "분석 결과: $analysisResult") // 결과를 로그에 출력
            val kcalValue = analysisResult.toIntOrNull()?.toString() ?: "" // Int를 String으로 변환
            updateKcal(kcalValue) // 분석 결과를 업데이트
        }?.onFailure { exception ->
            Log.e("AnalyzeResult", "분석 실패: ${exception.message}") // 실패 로그 출력
        }
    } else {
        Log.e("AnalyzeResult", "이미지 URL이 null입니다.") // 이미지 URL이 null일 경우
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
            if (exception.message != "Loading") { // Loading 상태가 아닌 경우만 표시
                Text(
                    text = "분석 실패: ${exception.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
fun CategoryDropdown(category: String, onCategorySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) } // 드롭다운 상태 관리
    val items = listOf("과자", "음료수", "사탕/젤리", "아이스크림") // 드롭다운 아이템

    // 버튼과 드롭다운 메뉴를 포함하는 Box
    Box(modifier = Modifier.fillMaxWidth()) {
        // OutlinedButton으로 드롭다운 트리거
        OutlinedButton(
            onClick = { expanded = !expanded }, // 클릭 시 상태 토글
            modifier = Modifier
                .fillMaxWidth() // 버튼 크기를 화면 너비에 맞춤
                .height(56.dp), // 버튼 높이 설정
            shape = MaterialTheme.shapes.small.copy(CornerSize(0.dp)), // 사각형 모양
            colors = ButtonDefaults.outlinedButtonColors() // 버튼 색상 기본값 유지
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = category.ifEmpty { "카테고리 선택" }, // 기본 텍스트 또는 선택된 카테고리
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.drop_up else R.drawable.drop_down
                    ),
                    contentDescription = "Dropdown Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f) // 드롭다운 메뉴가 화면의 90% 너비를 차지하도록 설정
                .padding(horizontal = 16.dp) // 드롭다운 메뉴 좌우에 추가 패딩
                .align(Alignment.TopCenter) // 버튼과 중심을 맞춤
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onCategorySelected(item) // 선택된 값 전달
                        expanded = false // 드롭다운 닫기
                    },
                    text = {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AnalyzeImageButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = { if (!isLoading) onClick() }, // 로딩 중일 때 클릭 방지
        shape = MaterialTheme.shapes.small.copy(CornerSize(0.dp)) // 사각형 모양
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp), // 로딩 아이콘 크기
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary // 로딩 아이콘 색상
            )
        } else {
            Text("사진")
        }
    }
}

@Composable
fun AnalyzeNameButton(
    isLoading: Boolean,
    name: String,
    onClick: () -> Unit
) {
    Button(
        onClick = { if (!isLoading) onClick() }, // 로딩 중에는 클릭 방지
        shape = MaterialTheme.shapes.small.copy(CornerSize(0.dp)), // 사각형 모양
        enabled = name.isNotEmpty() && !isLoading // 이름이 비어있지 않고 로딩 중이 아닐 때만 활성화
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp), // 로딩 아이콘 크기
                strokeWidth = 2.dp,             // 로딩 아이콘 두께
                color = MaterialTheme.colorScheme.onPrimary // 로딩 아이콘 색상
            )
        } else {
            Text("제품명")
        }
    }
}


