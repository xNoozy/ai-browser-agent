package com.qalab.launcher.service.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.qalab.launcher.domain.model.BatteryMetrics
import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.Severity
import com.qalab.launcher.domain.repository.MonitoringRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitoringRepository: MonitoringRepository
) {
    private val batteryManager: BatteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private var initialLevel: Int = -1
    private var startTimestamp: Long = 0L
    private val batteryHistory = mutableListOf<BatteryMetrics>()

    suspend fun start(sessionId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            initialLevel = getBatteryLevel()
            startTimestamp = System.currentTimeMillis()

            while (coroutineContext.isActive) {
                val metrics = collectBatteryMetrics()
                batteryHistory.add(metrics)

                // Check for excessive battery drain
                if (batteryHistory.size >= 2) {
                    val drainRate = calculateDrainRate()
                    if (drainRate > HIGH_DRAIN_THRESHOLD) {
                        val log = MonitoringLog(
                            sessionId = sessionId,
                            packageName = packageName,
                            logType = LogType.BATTERY,
                            severity = Severity.WARNING,
                            message = "High battery drain rate: ${String.format("%.1f", drainRate)}%/hour",
                            metadata = mapOf(
                                "level" to metrics.level.toString(),
                                "temperature" to "${metrics.temperature}°C",
                                "isCharging" to metrics.isCharging.toString(),
                                "drainRatePerHour" to String.format("%.2f", drainRate)
                            )
                        )
                        monitoringRepository.insertLog(log)
                    }
                }

                // Periodic status log
                if (batteryHistory.size % 12 == 0) { // Every minute
                    val log = MonitoringLog(
                        sessionId = sessionId,
                        packageName = packageName,
                        logType = LogType.BATTERY,
                        severity = Severity.INFO,
                        message = "Battery: ${metrics.level}%, Temp: ${metrics.temperature}°C",
                        metadata = mapOf(
                            "level" to metrics.level.toString(),
                            "temperature" to metrics.temperature.toString(),
                            "isCharging" to metrics.isCharging.toString(),
                            "totalDrain" to "${initialLevel - metrics.level}%"
                        )
                    )
                    monitoringRepository.insertLog(log)
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun collectBatteryMetrics(): BatteryMetrics {
        val level = getBatteryLevel()
        val temperature = getBatteryTemperature()
        val isCharging = batteryManager.isCharging

        return BatteryMetrics(
            level = level,
            temperature = temperature,
            isCharging = isCharging,
            drainRatePerHour = calculateDrainRate()
        )
    }

    private fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getBatteryTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f
    }

    private fun calculateDrainRate(): Float {
        if (batteryHistory.size < 2) return 0f

        val first = batteryHistory.first()
        val last = batteryHistory.last()
        val timeDiffHours = (last.timestamp - first.timestamp) / 3600000f

        if (timeDiffHours <= 0) return 0f

        return (first.level - last.level) / timeDiffHours
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5000L
        private const val HIGH_DRAIN_THRESHOLD = 15f // %/hour
    }
}
