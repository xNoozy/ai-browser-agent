package com.qalab.launcher.domain.usecase

import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.MonitoringStats
import com.qalab.launcher.domain.repository.MonitoringRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonitoringLogsUseCase @Inject constructor(
    private val repository: MonitoringRepository
) {
    operator fun invoke(sessionId: String): Flow<List<MonitoringLog>> {
        return repository.getLogsBySession(sessionId)
    }

    fun byType(sessionId: String, type: LogType): Flow<List<MonitoringLog>> {
        return repository.getLogsByType(sessionId, type)
    }
}

class GetRecentErrorsUseCase @Inject constructor(
    private val repository: MonitoringRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<MonitoringLog>> {
        return repository.getRecentErrors(limit)
    }
}

class GetMonitoringStatsUseCase @Inject constructor(
    private val repository: MonitoringRepository
) {
    suspend operator fun invoke(sessionId: String): MonitoringStats {
        return MonitoringStats(
            totalCrashes = repository.getCountByType(sessionId, LogType.CRASH),
            totalAnrs = repository.getCountByType(sessionId, LogType.ANR),
            logcatErrors = repository.getCountByType(sessionId, LogType.LOGCAT),
            permissionRequests = repository.getCountByType(sessionId, LogType.PERMISSION)
        )
    }
}

class InsertMonitoringLogUseCase @Inject constructor(
    private val repository: MonitoringRepository
) {
    suspend operator fun invoke(log: MonitoringLog): Long {
        return repository.insertLog(log)
    }
}
