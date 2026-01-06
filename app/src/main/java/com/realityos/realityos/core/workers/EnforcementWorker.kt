package com.realityos.realityos.core.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.realityos.realityos.core.services.RealityAccessibilityService

class EnforcementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // This is a temporary diagnostic worker.
        // Its only job is to log that it ran and ensure the block is turned off.
        // This proves the worker is not the source of the crash.
        Log.d("RealityOS_Worker", "Diagnostic Worker Ran. Setting isBlockActive to false.")
        
        RealityAccessibilityService.isBlockActive.value = false

        return Result.success()
    }
}
