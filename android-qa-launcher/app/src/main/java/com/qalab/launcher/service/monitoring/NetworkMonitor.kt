package com.qalab.launcher.service.monitoring

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
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

class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitoringRepository: MonitoringRepository
) {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var previousRxBytes = 0L
    private var previousTxBytes = 0L
    private var previousTimestamp = 0L

    suspend fun start(sessionId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            registerNetworkCallback(sessionId, packageName)

            previousRxBytes = TrafficStats.getTotalRxBytes()
            previousTxBytes = TrafficStats.getTotalTxBytes()
            previousTimestamp = System.currentTimeMillis()

            while (coroutineContext.isActive) {
                measureNetworkUsage(sessionId, packageName)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun measureNetworkUsage(sessionId: String, packageName: String) {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTimestamp = System.currentTimeMillis()

        val deltaTime = currentTimestamp - previousTimestamp
        if (deltaTime <= 0) return

        val rxRate = ((currentRxBytes - previousRxBytes) * 1000) / deltaTime // bytes/sec
        val txRate = ((currentTxBytes - previousTxBytes) * 1000) / deltaTime // bytes/sec

        if (rxRate > HIGH_TRAFFIC_THRESHOLD || txRate > HIGH_TRAFFIC_THRESHOLD) {
            val log = MonitoringLog(
                sessionId = sessionId,
                packageName = packageName,
                logType = LogType.NETWORK,
                severity = Severity.WARNING,
                message = "High network usage detected",
                metadata = mapOf(
                    "rxRate" to "${rxRate / 1024} KB/s",
                    "txRate" to "${txRate / 1024} KB/s",
                    "totalRx" to "${currentRxBytes / (1024 * 1024)} MB",
                    "totalTx" to "${currentTxBytes / (1024 * 1024)} MB"
                )
            )
            monitoringRepository.insertLog(log)
        }

        previousRxBytes = currentRxBytes
        previousTxBytes = currentTxBytes
        previousTimestamp = currentTimestamp
    }

    private fun registerNetworkCallback(sessionId: String, packageName: String) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    kotlinx.coroutines.runBlocking {
                        val log = MonitoringLog(
                            sessionId = sessionId,
                            packageName = packageName,
                            logType = LogType.NETWORK,
                            severity = Severity.WARNING,
                            message = "Network connection lost"
                        )
                        monitoringRepository.insertLog(log)
                    }
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    val downSpeed = capabilities.linkDownstreamBandwidthKbps
                    val upSpeed = capabilities.linkUpstreamBandwidthKbps

                    if (downSpeed < LOW_BANDWIDTH_THRESHOLD) {
                        kotlinx.coroutines.runBlocking {
                            val log = MonitoringLog(
                                sessionId = sessionId,
                                packageName = packageName,
                                logType = LogType.NETWORK,
                                severity = Severity.INFO,
                                message = "Low bandwidth detected: ${downSpeed}kbps down, ${upSpeed}kbps up",
                                metadata = mapOf(
                                    "downstreamKbps" to downSpeed.toString(),
                                    "upstreamKbps" to upSpeed.toString()
                                )
                            )
                            monitoringRepository.insertLog(log)
                        }
                    }
                }
            }
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5000L
        private const val HIGH_TRAFFIC_THRESHOLD = 1024 * 1024L // 1 MB/s
        private const val LOW_BANDWIDTH_THRESHOLD = 1000 // 1 Mbps
    }
}
