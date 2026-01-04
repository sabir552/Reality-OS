package com.realityos.realityos.ui.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realityos.realityos.ui.AppViewModelProvider

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    var usagePermissionGranted by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var accessibilityPermissionGranted by remember { mutableStateOf(hasAccessibilityPermission(context)) }

    val allPermissionsGranted = usagePermissionGranted && accessibilityPermissionGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome to Reality OS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "To enforce your rules, Reality OS requires two special permissions. Your data is processed locally and is never shared.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            PermissionCard(
                title = "Usage Stats Access",
                description = "Allows the app to track which app is in the foreground and for how long.",
                granted = usagePermissionGranted,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
            PermissionCard(
                title = "Accessibility Service",
                description = "Allows the app to apply punishments like greyscale or blocking overlays.",
                granted = accessibilityPermissionGranted,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )
        }

        Button(
            onClick = {
                viewModel.completeOnboarding()
                onOnboardingComplete()
            },
            enabled = allPermissionsGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BEGIN COMMITMENT")
        }
    }
}

@Composable
fun PermissionCard(title: String, description: String, granted: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(
             containerColor = if (granted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
         )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            if (!granted) {
                Button(onClick = onClick, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                    Text("Grant")
                }
            }
        }
    }
}


private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

private fun hasAccessibilityPermission(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    return enabledServices?.contains(context.packageName) ?: false
}
