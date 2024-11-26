package com.example.termproject.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import com.example.termproject.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProductScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Select")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background // 원하는 색으로 변경
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    TextButton(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { navController.navigate("main") }) {
                        Text(text = "Select")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate("camera")}) {
                Icon(imageVector = ImageVector.vectorResource(id =R.drawable.baseline_camera_alt_24), contentDescription = "")
            }
        }
    ) { innerpadding ->
        Column (
            modifier = Modifier.padding(innerpadding)
        ){
            Text("대충 물품 선택")
            // 여기에다가 파이어베이스 연동시켜서 물품 product
        }
    }
}
