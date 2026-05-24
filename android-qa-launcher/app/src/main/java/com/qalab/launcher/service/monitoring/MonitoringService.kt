package com.qalab.launcher.service.monitoring

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.qalab.launcher.QALabApplication
import com.qalab.launcher.R
import com.qalab.launcher.domain.model.LogType
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.model.Severity
import com.qalab.launcher.domain.repository.MonitoringRepository
import com.qalab.launcher.domain.repository.SessionRepository
import com.qalab.launcher.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject lateinit var monitoringRepository: MonitoringRepository
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var crashMonitor: CrashMonitor
    @Inject lateinit var logcatMonitor: LogcatMonitor
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var batteryMonitor: BatteryMonitor
    @Inject lateinit var permissionMonitor: PermissionMonitor

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return START_NOT_STICKY

        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring(sessionId, packageName)

        return START_STICKY
    }

    private fun startMonitoring(sessionId: String, packageName: String) {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            // Start all monitors concurrently
            launch { crashMonitor.start(sessionId, packageName) }
            launch { logcatMonitor.start(sessionId, packageName) }
            launch { networkMonitor.start(sessionId, packageName) }
            launch { batteryMonitor.start(sessionId, packageName) }
            launch { permissionMonitor.start(sessionId, packageName) }
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, QALabApplication.CHANNEL_MONITORING)
            .setContentTitle(getString(R.string.notification_monitoring_title))
            .setContentText(getString(R.string.notification_monitoring_text))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
        private const val NOTIFICATION_ID = 1001

        fun createIntent(
            context: android.content.Context,
            sessionId: String,
            packageName: String
        ): Intent {
            return Intent(context, MonitoringService::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
        }
    }
}
