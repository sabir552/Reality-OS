package com.realityos.realityos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryEventDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistoryEvents(): Flow<List<HistoryEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: HistoryEventEntity)
}
