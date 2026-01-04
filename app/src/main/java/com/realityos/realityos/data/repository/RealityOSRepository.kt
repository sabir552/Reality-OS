package com.realityos.realityos.data.repository

import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.local.entity.RuleEntity
import com.realityos.realityos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface RealityOSRepository {
    fun getUser(): Flow<UserEntity?>
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)

    fun getRules(): Flow<List<RuleEntity>>
    suspend fun addRule(rule: RuleEntity)
    suspend fun deleteRule(rule: RuleEntity)

    fun getHistory(): Flow<List<HistoryEventEntity>>
    suspend fun logHistoryEvent(event: HistoryEventEntity)
}
