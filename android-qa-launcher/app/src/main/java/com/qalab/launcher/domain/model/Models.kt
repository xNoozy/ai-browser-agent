package com.qalab.launcher.domain.model

import java.util.UUID

data class TestSession(
    val id: String = UUID.randomUUID().toString(),
    val targetPackage: String,
    val targetAppName: String,
    val deviceProfileId: String? = null,
    val status: SessionStatus = SessionStatus.CREATED,
    val workProfileId: Int? = null,
    val sandboxConfig: SandboxConfig = SandboxConfig(),
    val crashCount: Int = 0,
    val anrCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

enum class SessionStatus {
    CREATED, RUNNING, PAUSED, STOPPED, CRASHED
}

data class SandboxConfig(
    val isolateStorage: Boolean = true,
    val isolateNetwork: Boolean = false,
    val mockLocation: Boolean = false,
    val clearDataOnStop: Boolean = false,
    val restrictPermissions: List<String> = emptyList()
)

data class MonitoringLog(
    val id: Long = 0,
    val sessionId: String,
    val packageName: String,
    val logType: LogType,
    val severity: Severity,
    val message: String,
    val stackTrace: String? = null,
    val metadata: Map<String, String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogType {
    CRASH, ANR, LOGCAT, NETWORK, BATTERY, PERMISSION
}

enum class Severity {
    INFO, WARNING, ERROR, CRITICAL
}

data class DeviceProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val manufacturer: String,
    val model: String,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val densityDpi: Int,
    val sdkVersion: Int,
    val locale: String = "en_US",
    val orientation: Orientation = Orientation.PORTRAIT,
    val fontScale: Float = 1.0f,
    val isNightMode: Boolean = false,
    val networkType: NetworkType = NetworkType.WIFI,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class Orientation { PORTRAIT, LANDSCAPE }

enum class NetworkType { WIFI, LTE, HSPA, EDGE, OFFLINE }

data class MonitoringStats(
    val totalCrashes: Int = 0,
    val totalAnrs: Int = 0,
    val avgNetworkLatency: Long = 0,
    val batteryDrain: Float = 0f,
    val permissionRequests: Int = 0,
    val logcatErrors: Int = 0
)

data class NetworkMetrics(
    val url: String,
    val method: String,
    val statusCode: Int,
    val latencyMs: Long,
    val requestSize: Long,
    val responseSize: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class BatteryMetrics(
    val level: Int,
    val temperature: Float,
    val isCharging: Boolean,
    val drainRatePerHour: Float,
    val timestamp: Long = System.currentTimeMillis()
)
