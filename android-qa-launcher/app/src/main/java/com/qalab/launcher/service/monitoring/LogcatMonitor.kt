package com.qalab.launcher.service.monitoring

import android.content.Context
import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.Severity
import com.qalab.launcher.domain.repository.MonitoringRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class LogcatMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitoringRepository: MonitoringRepository
) {
    suspend fun start(sessionId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            try {
                // Clear logcat buffer for target package then monitor
                Runtime.getRuntime().exec(arrayOf("logcat", "-c"))

                val process = Runtime.getRuntime().exec(
                    arrayOf("logcat", "-v", "time", "--pid=$(pidof $packageName)", "*:W")
                )

                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (coroutineContext.isActive) {
                    val line = reader.readLine() ?: break
                    processLogLine(sessionId, packageName, line)
                }

                process.destroy()
            } catch (e: SecurityException) {
                // READ_LOGS permission not granted, use alternative monitoring
                monitorLogcatAlternative(sessionId, packageName)
            }
        }
    }

    private suspend fun monitorLogcatAlternative(sessionId: String, packageName: String) {
        // Fallback: monitor app's own logcat output via ApplicationExitInfo
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE)
                as android.app.ActivityManager
            // Use ApplicationExitInfo API (Android 11+)
            val exitInfos = am.getHistoricalProcessExitReasons(packageName, 0, 10)
            exitInfos.forEach { info ->
                val severity = when (info.reason) {
                    android.app.ApplicationExitInfo.REASON_CRASH,
                    android.app.ApplicationExitInfo.REASON_CRASH_NATIVE -> Severity.CRITICAL
                    android.app.ApplicationExitInfo.REASON_ANR -> Severity.CRITICAL
                    android.app.ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> Severity.WARNING
                    else -> Severity.INFO
                }

                val log = MonitoringLog(
                    sessionId = sessionId,
                    packageName = packageName,
                    logType = LogType.LOGCAT,
                    severity = severity,
                    message = "Exit reason: ${info.description ?: getReasonString(info.reason)}",
                    metadata = mapOf(
                        "reason" to info.reason.toString(),
                        "importance" to info.importance.toString(),
                        "pss" to info.pss.toString(),
                        "rss" to info.rss.toString()
                    )
                )
                monitoringRepository.insertLog(log)
            }
        } catch (e: Exception) {
            // Silently handle - monitoring continues with other monitors
        }
    }

    private suspend fun processLogLine(sessionId: String, packageName: String, line: String) {
        val severity = when {
            line.contains(" E/") || line.contains(" E ") -> Severity.ERROR
            line.contains(" W/") || line.contains(" W ") -> Severity.WARNING
            else -> Severity.INFO
        }

        if (severity == Severity.ERROR || severity == Severity.WARNING) {
            val log = MonitoringLog(
                sessionId = sessionId,
                packageName = packageName,
                logType = LogType.LOGCAT,
                severity = severity,
                message = line.take(500)
            )
            monitoringRepository.insertLog(log)
        }
    }

    private fun getReasonString(reason: Int): String {
        return when (reason) {
            android.app.ApplicationExitInfo.REASON_CRASH -> "Crash"
            android.app.ApplicationExitInfo.REASON_CRASH_NATIVE -> "Native Crash"
            android.app.ApplicationExitInfo.REASON_ANR -> "ANR"
            android.app.ApplicationExitInfo.REASON_LOW_MEMORY -> "Low Memory"
            android.app.ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "Excessive Resources"
            android.app.ApplicationExitInfo.REASON_USER_REQUESTED -> "User Requested"
            android.app.ApplicationExitInfo.REASON_USER_STOPPED -> "User Stopped"
            else -> "Unknown ($reason)"
        }
    }
}
