package com.qalab.launcher.data.repository

import com.qalab.launcher.data.local.dao.MonitoringLogDao
import com.qalab.launcher.data.local.entity.MonitoringLogEntity
import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.Severity
import com.qalab.launcher.domain.repository.MonitoringRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonitoringRepositoryImpl @Inject constructor(
    private val dao: MonitoringLogDao
) : MonitoringRepository {

    private val gson = Gson()

    override fun getLogsBySession(sessionId: String): Flow<List<MonitoringLog>> {
        return dao.getLogsBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLogsByType(sessionId: String, type: LogType): Flow<List<MonitoringLog>> {
        return dao.getLogsByType(sessionId, type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentErrors(limit: Int): Flow<List<MonitoringLog>> {
        return dao.getRecentErrors(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentLogs(limit: Int): Flow<List<MonitoringLog>> {
        return dao.getRecentLogs(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertLog(log: MonitoringLog): Long {
        return dao.insert(log.toEntity())
    }

    override suspend fun insertLogs(logs: List<MonitoringLog>) {
        dao.insertAll(logs.map { it.toEntity() })
    }

    override suspend fun getCountByType(sessionId: String, type: LogType): Int {
        return dao.getCountByType(sessionId, type.name)
    }

    override suspend fun deleteBySession(sessionId: String) {
        dao.deleteBySession(sessionId)
    }

    override suspend fun deleteOldLogs(before: Long) {
        dao.deleteOldLogs(before)
    }

    private fun MonitoringLogEntity.toDomain(): MonitoringLog {
        val metadataMap: Map<String, String>? = metadata?.let {
            gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type)
        }
        return MonitoringLog(
            id = id,
            sessionId = sessionId,
            packageName = packageName,
            logType = LogType.valueOf(logType),
            severity = Severity.valueOf(severity),
            message = message,
            stackTrace = stackTrace,
            metadata = metadataMap,
            timestamp = timestamp
        )
    }

    private fun MonitoringLog.toEntity(): MonitoringLogEntity {
        return MonitoringLogEntity(
            id = id,
            sessionId = sessionId,
            packageName = packageName,
            logType = logType.name,
            severity = severity.name,
            message = message,
            stackTrace = stackTrace,
            metadata = metadata?.let { gson.toJson(it) },
            timestamp = timestamp
        )
    }
}
