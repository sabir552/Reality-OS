package com.realityos.realityos

import android.app.Application
import androidx.work.Configuration
import com.realityos.realityos.data.AppContainer
import com.realityos.realityos.data.AppDataContainer

class RealityOSApplication : Application(), Configuration.Provider {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
