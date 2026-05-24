package com.qalab.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitoring_logs")
data class MonitoringLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val packageName: String,
    val logType: String, // CRASH, ANR, LOGCAT, NETWORK, BATTERY, PERMISSION
    val severity: String, // INFO, WARNING, ERROR, CRITICAL
    val message: String,
    val stackTrace: String? = null,
    val metadata: String? = null, // JSON metadata
    val timestamp: Long = System.currentTimeMillis()
)
