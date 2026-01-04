package com.realityos.realityos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.realityos.realityos.data.local.dao.HistoryEventDao
import com.realityos.realityos.data.local.dao.RuleDao
import com.realityos.realityos.data.local.dao.UserDao
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.local.entity.RuleEntity
import com.realityos.realityos.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, RuleEntity::class, HistoryEventEntity::class], version = 1, exportSchema = false)
abstract class RealityOSDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun ruleDao(): RuleDao
    abstract fun historyEventDao(): HistoryEventDao

    companion object {
        @Volatile
        private var Instance: RealityOSDatabase? = null

        fun getDatabase(context: Context): RealityOSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, RealityOSDatabase::class.java, "reality_os_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
