package com.realityos.realityos.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.realityos.realityos.ui.AppViewModelProvider

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("REALITY OS") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = homeUiState) {
                is HomeUiState.Success -> {
                    StatusDashboard(
                        level = state.user.commitmentLevel,
                        streak = state.user.streak,
                        xp = state.user.xp
                    )
                }
                HomeUiState.Loading -> {
                    CircularProgressIndicator()
                }
            }

            NavigationGrid(navController)
        }
    }
}

@Composable
fun StatusDashboard(level: String, streak: Int, xp: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatusItem("LEVEL", level)
            StatusItem("STREAK", streak.toString())
            StatusItem("XP", xp.toString())
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NavigationGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NavButton("Manage Rules", onClick = { navController.navigate("rules") })
        NavButton("View History", onClick = { navController.navigate("history") })
        NavButton("Commitment Modes", onClick = { navController.navigate("modes") })
        NavButton("Settings", onClick = { navController.navigate("settings") })
    }
}

@Composable
fun NavButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(text)
    }
}
