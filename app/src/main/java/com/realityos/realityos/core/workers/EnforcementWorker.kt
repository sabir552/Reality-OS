package com.realityos.realityos.core.workers

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.realityos.realityos.RealityOSApplication
import com.realityos.realityos.core.services.RealityAccessibilityService
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class EnforcementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository: RealityOSRepository = (applicationContext as RealityOSApplication).container.realityOSRepository

    override suspend fun doWork(): Result {
        Log.d("EnforcementWorker", "Worker starting...")
        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val foregroundApp = RealityAccessibilityService.currentForegroundApp.value

        // If no app is in the foreground, ensure blocking is off and exit
        if (foregroundApp == null) {
            RealityAccessibilityService.isBlockActive.value = false
            return Result.success()
        }
        
        // Get the specific rule for the foreground app, if it exists
        val ruleForApp = repository.getRules().first().find { it.targetAppPackageName == foregroundApp }

        // If there's no rule for this app, ensure blocking is off and exit
        if (ruleForApp == null) {
            RealityAccessibilityService.isBlockActive.value = false
            return Result.success()
        }

        // --- If we get here, there IS a rule for the current app ---

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // Get today's usage for this specific app
        val appUsage = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .firstOrNull { it.packageName == foregroundApp }
            ?.totalTimeInForeground ?: 0L
        
        val usageMinutes = appUsage / (1000 * 60)
        val timeLimitMinutes = ruleForApp.timeLimitMinutes

        Log.d("EnforcementWorker", "Checking rule for $foregroundApp. Usage: $usageMinutes min. Limit: $timeLimitMinutes min.")

        // Check if usage has exceeded the limit and the punishment is BLOCK
        if (usageMinutes >= timeLimitMinutes && ruleForApp.punishmentType == "BLOCK") {
            if (!RealityAccessibilityService.isBlockActive.value) {
                logPunishment("BLOCK", foregroundApp)
            }
            RealityAccessibilityService.isBlockActive.value = true
        } else {
            // If usage is within limits OR the punishment type isn't BLOCK, turn blocking off
            RealityAccessibilityService.isBlockActive.value = false
        }

        return Result.success()
    }

    private suspend fun logPunishment(punishmentType: String, packageName: String) {
        repository.logHistoryEvent(
            HistoryEventEntity(
                timestamp = System.currentTimeMillis(),
                eventDescription = "Punishment '$punishmentType' triggered for $packageName."
            )
        )
    }
}
