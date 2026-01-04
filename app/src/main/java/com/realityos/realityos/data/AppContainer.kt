package com.realityos.realityos.data

import android.content.Context
import com.realityos.realityos.data.local.RealityOSDatabase
import com.realityos.realityos.data.repository.OfflineFirstRealityOSRepository
import com.realityos.realityos.data.repository.RealityOSRepository

interface AppContainer {
    val realityOSRepository: RealityOSRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val realityOSRepository: RealityOSRepository by lazy {
        OfflineFirstRealityOSRepository(
            userDao = RealityOSDatabase.getDatabase(context).userDao(),
            ruleDao = RealityOSDatabase.getDatabase(context).ruleDao(),
            historyEventDao = RealityOSDatabase.getDatabase(context).historyEventDao()
        )
    }
}
