package com.example.termproject.ui.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.termproject.R
import com.example.termproject.ui.model.DateData
import com.example.termproject.ui.model.User
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController,
    overallViewModel: OverallViewModel
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val dateDataList by overallViewModel.dateDataList.collectAsState()
    val currentUserId by overallViewModel.currentUserId.collectAsState()

    LaunchedEffect(selectedDate) {
        overallViewModel.fetchDataForSpecificDate(selectedDate.toString())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
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
                        IconButton(onClick = { /* Handle calendar month navigation */ }) {
                            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_calendar_month_24), contentDescription = "")
                        }
                        IconButton(onClick = { navController.navigate("main") }) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = { navController.navigate("stats") }) {
                            Icon(Icons.Default.Info, contentDescription = "Stats")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("selectProduct") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigation
            MonthNavigation(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            CalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            Text(
                text = "Selected Date: $selectedDate",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )

            val filteredDateDataList = dateDataList.filter { it.user_id == currentUserId }

            DateDataLazyColumn(dateDataList = filteredDateDataList, selectedDate = selectedDate)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthNavigation(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPreviousMonth) {
            Text("< Prev")
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = onNextMonth) {
            Text("Next >")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    Column {
        // Weekday labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Calendar days
        Column {
            var dayCounter = 1
            for (week in 0..5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayOfWeek in 0..6) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || dayCounter > daysInMonth) {
                            // Empty cell
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val date = currentMonth.atDay(dayCounter)
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .background(
                                        color = if (date == selectedDate) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayCounter",
                                    color = if (date == selectedDate) Color.White else Color.Black,
                                    fontSize = 16.sp
                                )
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DateDataLazyColumn(
    dateDataList: List<DateData>,
    selectedDate: LocalDate
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(dateDataList) { dateData ->
            CalendarDateDataCard(
                dateData = dateData,
                selectedDate = selectedDate
            )
        }
    }
}

@Composable
fun CalendarDateDataCard(
    dateData: DateData,
    selectedDate: LocalDate
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.surface
                )
            )),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Kcal Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Total Calories",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${dateData.kcal} kcal",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Remove the Select Date button and keep only the selected date
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selected Date: $selectedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
