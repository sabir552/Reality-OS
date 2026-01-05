package com.realityos.realityos.ui.elite

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.realityos.realityos.ui.AppViewModelProvider

@Composable
fun EliteLockedScreen(
    navController: NavController,
    viewModel: EliteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity

    Scaffold(
        topBar = { TopAppBar(title = { Text("Elite Commitment") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.isElite -> EliteStatusView()
                uiState.isQualified -> QualifiedView(uiState = uiState, onUnlockClick = { viewModel.launchBillingFlow(activity) })
                else -> NotQualifiedView()
            }

            uiState.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top=16.dp))
            }
        }
    }
}

@Composable
fun EliteStatusView() {
    Text("STATUS: ELITE", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Text("Your commitment is permanent.", textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun NotQualifiedView() {
    Text("NOT QUALIFIED", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        "Elite commitment is earned, not given. It requires a significant streak and dedication without breaking your commitment.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text("Requirements: 30-day streak, 10,000 XP, zero breaks.", style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun QualifiedView(uiState: EliteUiState, onUnlockClick: () -> Unit) {
    Text("YOU ARE QUALIFIED", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        "Unlocking Elite is an irreversible decision. You are paying for the risk and the permanent consequences of failure. There is no going back.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onUnlockClick,
        enabled = uiState.productDetails != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        // THIS IS THE CORRECTED LINE - it now uses productDetails
        val buttonText = uiState.productDetails?.oneTimePurchaseOfferDetails?.formattedPrice?.let { "UNLOCK ELITE ($it)" } ?: "Loading Price..."
        Text(buttonText)
    }
}
