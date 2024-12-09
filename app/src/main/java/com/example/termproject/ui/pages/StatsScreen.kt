package com.example.termproject.ui.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.termproject.R
import com.example.termproject.ui.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    overallViewModel: OverallViewModel
) {
    val userEatList by overallViewModel.userEatList.collectAsState()
    val currentUserId by overallViewModel.currentUserId.collectAsState()

    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // 오늘 먹은 간식 필터링
    val filteredUserEatList = userEatList.filter { user ->
        user.user_id == currentUserId && user.date == currentDate
    }

    // 오늘 먹은 간식 카테고리 통계
    val todayStatistics = calculateCategoryStatistics(filteredUserEatList)

    // 현재 계정 기준 전체 간식 카테고리 통계
    val totalStatistics = calculateCategoryStatistics(
        userEatList.filter { user -> user.user_id == currentUserId }
    )

    // ViewModel에서 데이터 가져오기
    LaunchedEffect(Unit) {
        overallViewModel.fetchUserEatData()
        overallViewModel.fetchKcalDataFromDate()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Stats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { navController.navigate("calendar") }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = "캘린더로 이동"
                            )
                        }
                        IconButton(onClick = { navController.navigate("main") }) {
                            Icon(Icons.Default.Home, contentDescription = "홈으로 이동")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Info, contentDescription = "통계 보기")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("selectProduct") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "새 항목 추가")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SnackStatisticsSection(
                    title = "오늘의 간식 통계",
                    statistics = todayStatistics,
                    snackDetails = todayStatistics
                )
            }

            item {
                SnackStatisticsSection(
                    title = "총 간식 통계",
                    statistics = totalStatistics,
                    snackDetails = totalStatistics
                )
            }
        }
    }
}

@Composable
fun SnackStatisticsSection(
    title: String,
    statistics: Map<String, Int>,
    snackDetails: Map<String, Int>?,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFf44336), // Red
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107)  // Yellow
    )

    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (statistics.isNotEmpty()) {
            // 원 그래프
            PieChart(
                data = statistics,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // 원 크기 고정
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 아래 네모 범례
            snackDetails?.entries?.toList()?.forEachIndexed { index, (category, count) ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp)
                    ) {
                        drawRect(color = colors[index % colors.size]) // 네모로 범례 표시
                    }
                    Text(
                        text = "$category: $count",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Text(
                text = "No data available.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PieChart(data: Map<String, Int>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    val proportions = data.values.map { it.toFloat() / total }
    val colors = listOf(
        Color(0xFFf44336), // Red
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107)  // Yellow
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f) // 정사각형 비율 유지
    ) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f

        var startAngle = 0f
        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = proportion * 360
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            )
            startAngle += sweepAngle
        }
    }
}


fun calculateCategoryStatistics(data: List<User>): Map<String, Int> {
    val categories = listOf("과자", "음료수", "사탕/젤리", "아이스크림") // 4개의 고정 카테고리
    return data
        .groupingBy { it.category }
        .eachCount()
        .filterKeys { it in categories } // 카테고리 4개로 제한
        .filter { it.value > 0 } // 0인 카테고리 제외
}
