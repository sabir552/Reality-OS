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
        val rules = repository.getRules().first()
        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        try {
            rules.forEach { rule ->
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                val appUsage = usageStats
                    .firstOrNull { it.packageName == rule.targetAppPackageName }
                    ?.totalTimeInForeground ?: 0L

                val usageMinutes = appUsage / (1000 * 60)

                Log.d("EnforcementWorker", "Checking rule for ${rule.targetAppPackageName}. Usage: $usageMinutes min. Limit: ${rule.timeLimitMinutes} min.")

                val currentForegroundApp = RealityAccessibilityService.currentForegroundApp.value
                if (currentForegroundApp == rule.targetAppPackageName && usageMinutes >= rule.timeLimitMinutes) {
                    applyPunishment(rule.punishmentType, rule.targetAppPackageName)
                } else {
                    // Check if a punishment is active for an app that is no longer in the foreground
                    // or if usage is back within limits (though this shouldn't happen without a reset)
                    if (currentForegroundApp != rule.targetAppPackageName) {
                        removePunishmentIfNeeded(rule.punishmentType)
                    }
                }
            }
             // If no rules are violated for the current app, ensure no punishments are active
            val currentApp = RealityAccessibilityService.currentForegroundApp.value
            val isCurrentAppPunished = rules.any { it.targetAppPackageName == currentApp && ( (usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                .firstOrNull { stats -> stats.packageName == it.targetAppPackageName }?.totalTimeInForeground ?: 0) / (1000 * 60) >= it.timeLimitMinutes ) }

            if (!isCurrentAppPunished) {
                 RealityAccessibilityService.isGreyscaleActive.value = false
                 RealityAccessibilityService.isBlockActive.value = false
            }

        } catch (e: Exception) {
            Log.e("EnforcementWorker", "Error during work execution", e)
            return Result.failure()
        }

        Log.d("EnforcementWorker", "Worker finished.")
        return Result.success()
    }

    private suspend fun applyPunishment(punishmentType: String, packageName: String) {
         Log.d("EnforcementWorker", "Applying punishment $punishmentType for $packageName")
        when (punishmentType) {
            "GRAYSCALE" -> RealityAccessibilityService.isGreyscaleActive.value = true
            "BLOCK" -> RealityAccessibilityService.isBlockActive.value = true
        }
        // Log the event
        repository.logHistoryEvent(
            HistoryEventEntity(
                timestamp = System.currentTimeMillis(),
                eventDescription = "Punishment '$punishmentType' triggered for $packageName."
            )
        )
    }

    private fun removePunishmentIfNeeded(punishmentType: String) {
         Log.d("EnforcementWorker", "Removing punishment $punishmentType if active.")
         when (punishmentType) {
            "GRAYSCALE" -> if (RealityAccessibilityService.isGreyscaleActive.value) RealityAccessibilityService.isGreyscaleActive.value = false
            "BLOCK" -> if (RealityAccessibilityService.isBlockActive.value) RealityAccessibilityService.isBlockActive.value = false
        }
    }
}
