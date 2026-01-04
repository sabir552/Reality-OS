package com.realityos.realityos.data.repository

import com.realityos.realityos.data.local.dao.HistoryEventDao
import com.realityos.realityos.data.local.dao.RuleDao
import com.realityos.realityos.data.local.dao.UserDao
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.local.entity.RuleEntity
import com.realityos.realityos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class OfflineFirstRealityOSRepository(
    private val userDao: UserDao,
    private val ruleDao: RuleDao,
    private val historyEventDao: HistoryEventDao
) : RealityOSRepository {

    override fun getUser(): Flow<UserEntity?> = userDao.getUser()
    override suspend fun insertUser(user: UserEntity) = userDao.insert(user)
    override suspend fun updateUser(user: UserEntity) = userDao.update(user)

    override fun getRules(): Flow<List<RuleEntity>> = ruleDao.getAllRules()
    override suspend fun addRule(rule: RuleEntity) = ruleDao.insert(rule)
    override suspend fun deleteRule(rule: RuleEntity) = ruleDao.delete(rule)

    override fun getHistory(): Flow<List<HistoryEventEntity>> = historyEventDao.getAllHistoryEvents()
    override suspend fun logHistoryEvent(event: HistoryEventEntity) = historyEventDao.insert(event)
}
