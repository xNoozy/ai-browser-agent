package com.qalab.launcher.domain.repository

import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun getLogsBySession(sessionId: String): Flow<List<MonitoringLog>>
    fun getLogsByType(sessionId: String, type: LogType): Flow<List<MonitoringLog>>
    fun getRecentErrors(limit: Int = 50): Flow<List<MonitoringLog>>
    fun getRecentLogs(limit: Int = 100): Flow<List<MonitoringLog>>
    suspend fun insertLog(log: MonitoringLog): Long
    suspend fun insertLogs(logs: List<MonitoringLog>)
    suspend fun getCountByType(sessionId: String, type: LogType): Int
    suspend fun deleteBySession(sessionId: String)
    suspend fun deleteOldLogs(before: Long)
}
