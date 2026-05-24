package com.qalab.launcher.service.monitoring

import android.app.ActivityManager
import android.content.Context
import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.Severity
import com.qalab.launcher.domain.repository.MonitoringRepository
import com.qalab.launcher.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class CrashMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitoringRepository: MonitoringRepository,
    private val sessionRepository: SessionRepository
) {
    private val activityManager: ActivityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    suspend fun start(sessionId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            // Set up uncaught exception handler for crash detection
            val previousProcesses = mutableSetOf<Int>()

            while (coroutineContext.isActive) {
                try {
                    checkForCrashes(sessionId, packageName, previousProcesses)
                    checkForAnr(sessionId, packageName)
                } catch (e: Exception) {
                    // Log monitoring error but continue
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkForCrashes(
        sessionId: String,
        packageName: String,
        previousProcesses: MutableSet<Int>
    ) {
        val currentProcesses = activityManager.runningAppProcesses
            ?.filter { it.processName.startsWith(packageName) }
            ?.map { it.pid }
            ?.toSet() ?: emptySet()

        if (previousProcesses.isNotEmpty()) {
            val disappeared = previousProcesses - currentProcesses
            if (disappeared.isNotEmpty() && currentProcesses.isEmpty()) {
                val log = MonitoringLog(
                    sessionId = sessionId,
                    packageName = packageName,
                    logType = LogType.CRASH,
                    severity = Severity.CRITICAL,
                    message = "App process terminated unexpectedly (possible crash)",
                    metadata = mapOf(
                        "previousPids" to disappeared.joinToString(","),
                        "timestamp" to System.currentTimeMillis().toString()
                    )
                )
                monitoringRepository.insertLog(log)
                sessionRepository.incrementCrashCount(sessionId)
            }
        }

        previousProcesses.clear()
        previousProcesses.addAll(currentProcesses)
    }

    private suspend fun checkForAnr(sessionId: String, packageName: String) {
        // Check ANR via ActivityManager error states
        val errorInfo = activityManager.processesInErrorState
        errorInfo?.forEach { info ->
            if (info.processName.startsWith(packageName)) {
                when (info.condition) {
                    ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING -> {
                        val log = MonitoringLog(
                            sessionId = sessionId,
                            packageName = packageName,
                            logType = LogType.ANR,
                            severity = Severity.CRITICAL,
                            message = "ANR detected: ${info.shortMsg}",
                            stackTrace = info.longMsg,
                            metadata = mapOf(
                                "pid" to info.pid.toString(),
                                "tag" to (info.tag ?: "unknown")
                            )
                        )
                        monitoringRepository.insertLog(log)
                        sessionRepository.incrementAnrCount(sessionId)
                    }
                    ActivityManager.ProcessErrorStateInfo.CRASHED -> {
                        val log = MonitoringLog(
                            sessionId = sessionId,
                            packageName = packageName,
                            logType = LogType.CRASH,
                            severity = Severity.CRITICAL,
                            message = "Crash detected: ${info.shortMsg}",
                            stackTrace = info.longMsg,
                            metadata = mapOf(
                                "pid" to info.pid.toString(),
                                "tag" to (info.tag ?: "unknown")
                            )
                        )
                        monitoringRepository.insertLog(log)
                        sessionRepository.incrementCrashCount(sessionId)
                    }
                }
            }
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 2000L
    }
}
