package com.realityos.realityos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetAppPackageName: String,
    val timeLimitMinutes: Long,
    val punishmentType: String // GRAYSCALE, BLOCK
)
