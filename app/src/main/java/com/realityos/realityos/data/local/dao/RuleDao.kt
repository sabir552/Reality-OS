package com.realityos.realityos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.realityos.realityos.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY targetAppPackageName ASC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: RuleEntity)

    @Delete
    suspend fun delete(rule: RuleEntity)
}
