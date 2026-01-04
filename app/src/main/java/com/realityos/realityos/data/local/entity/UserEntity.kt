package com.realityos.realityos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 1, // Singleton user
    val commitmentLevel: String, // INITIATE, DISCIPLINED, ELITE
    val xp: Long,
    val streak: Int,
    val hasBrokenCommitment: Boolean,
    val installTimestamp: Long
)
