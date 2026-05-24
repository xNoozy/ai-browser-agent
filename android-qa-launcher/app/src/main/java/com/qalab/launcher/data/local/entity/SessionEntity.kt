package com.qalab.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val targetPackage: String,
    val targetAppName: String,
    val deviceProfileId: String? = null,
    val status: String, // CREATED, RUNNING, PAUSED, STOPPED, CRASHED
    val workProfileId: Int? = null,
    val sandboxConfig: String? = null, // JSON config
    val crashCount: Int = 0,
    val anrCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)
