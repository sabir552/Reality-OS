package com.realityos.realityos

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.realityos.realityos.core.workers.EnforcementWorker
import com.realityos.realityos.data.repository.RealityOSRepository
import com.realityos.realityos.ui.RealityOSApp
import com.realityos.realityos.ui.theme.RealityOSTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var repository: RealityOSRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (application as RealityOSApplication).container.realityOSRepository

        lifecycleScope.launch {
            val user = repository.getUser().first()
            val startDestination = if (user == null || !hasAllPermissions(this@MainActivity)) "onboarding" else "home"
            setContent {
                RealityOSTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RealityOSApp(startDestination = startDestination)
                    }
                }
            }
        }

        scheduleWorker()
    }

    private fun scheduleWorker() {
        val enforcementWorkRequest = PeriodicWorkRequestBuilder<EnforcementWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RuleEnforcement",
            ExistingPeriodicWorkPolicy.KEEP,
            enforcementWorkRequest
        )
    }

    private fun hasAllPermissions(context: Context): Boolean {
        val usageStatsEnabled = try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }

        val accessibilityEnabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(context.packageName) ?: false

        return usageStatsEnabled && accessibilityEnabled
    }
}
