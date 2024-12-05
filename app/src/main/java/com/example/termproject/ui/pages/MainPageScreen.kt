package com.example.termproject.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.termproject.R
import com.example.termproject.ui.model.DateData
import com.example.termproject.ui.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageScreen(
    navController: NavController,
    overallViewModel: OverallViewModel // ViewModel 추가
) {
    val userEatList by overallViewModel.userEatList.collectAsState()
    val currentUserId by overallViewModel.currentUserId.collectAsState()
    val dateDataList by overallViewModel.dateDataList.collectAsState()

    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val filteredUserEatList = userEatList.filter { user ->
        user.user_id == currentUserId && user.date == currentDate
    }

    val filteredDateDataList = dateDataList.filter { dateData ->
        dateData.user_id == currentUserId && dateData.date == currentDate // user_id와 날짜 조건
    }

    // 화면 로딩 시 데이터 가져오기
    LaunchedEffect(Unit) {
        overallViewModel.fetchUserEatData()
        overallViewModel.fetchKcalDataFromDate()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "MainPage") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = {
                        overallViewModel.logout() // 로그아웃 실행
                        navController.navigate("login") { // 로그인 화면으로 이동
                            popUpTo("main") { inclusive = true } // 현재 화면 제거
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "logout"
                        )
                    }
                }
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
                        IconButton(onClick = {
                            navController.navigate("calendar") {
                                popUpTo("main") { inclusive = true }
                            }
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = "캘린더로 이동"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Home, contentDescription = "홈으로 이동")
                        }
                        IconButton(onClick = {
                            navController.navigate("stats") {
                                popUpTo("main") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "통계 보기")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("selectProduct"){
                    popUpTo("main") { inclusive = true }
                }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "새 항목 추가")
            }
        }
    )  { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Today's stats",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )

            DateDataList(dateDataList = filteredDateDataList) // 필터링된 리스트 전달

            UserEatList(userEatList = filteredUserEatList) // 필터링된 리스트 전달
        }
    }
}

@Composable
fun UserEatList(userEatList: List<User>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(userEatList) { user ->
            UserCard(user)
        }
    }
}

@Composable
fun UserCard(user: User) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            // 이미지 추가
            AsyncImage(
                model = user.imageurl,
                contentDescription = "User Image",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )

            // 텍스트 정보
            Column {
                Text(text = "Name: ${user.name}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Category: ${user.category}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Kcal: ${user.kcal}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: ${user.date}", style = MaterialTheme.typography.bodySmall)
                Text(text = "User id: ${user.user_id}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DateDataList(dateDataList: List<DateData>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(dateDataList) { dateData ->
            DateDataCard(dateData)
        }
    }
}

@Composable
fun DateDataCard(dateData: DateData) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column {
                Text(text = "User ID: ${dateData.user_id}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Kcal: ${dateData.kcal}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: ${dateData.date}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}