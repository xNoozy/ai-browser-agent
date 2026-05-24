package com.qalab.launcher.service.monitoring

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
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

class PermissionMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitoringRepository: MonitoringRepository
) {
    private val packageManager: PackageManager = context.packageManager
    private val appOpsManager: AppOpsManager =
        context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    private var previousPermissions: Set<String> = emptySet()

    suspend fun start(sessionId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            previousPermissions = getGrantedPermissions(packageName)

            // Log initial permissions state
            val log = MonitoringLog(
                sessionId = sessionId,
                packageName = packageName,
                logType = LogType.PERMISSION,
                severity = Severity.INFO,
                message = "Initial permissions: ${previousPermissions.size} granted",
                metadata = mapOf(
                    "permissions" to previousPermissions.joinToString(", ")
                )
            )
            monitoringRepository.insertLog(log)

            while (coroutineContext.isActive) {
                checkPermissionChanges(sessionId, packageName)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkPermissionChanges(sessionId: String, packageName: String) {
        val currentPermissions = getGrantedPermissions(packageName)

        // Check for newly granted permissions
        val newlyGranted = currentPermissions - previousPermissions
        newlyGranted.forEach { permission ->
            val log = MonitoringLog(
                sessionId = sessionId,
                packageName = packageName,
                logType = LogType.PERMISSION,
                severity = Severity.INFO,
                message = "Permission granted: ${formatPermissionName(permission)}",
                metadata = mapOf(
                    "permission" to permission,
                    "action" to "granted"
                )
            )
            monitoringRepository.insertLog(log)
        }

        // Check for revoked permissions
        val revoked = previousPermissions - currentPermissions
        revoked.forEach { permission ->
            val log = MonitoringLog(
                sessionId = sessionId,
                packageName = packageName,
                logType = LogType.PERMISSION,
                severity = Severity.WARNING,
                message = "Permission revoked: ${formatPermissionName(permission)}",
                metadata = mapOf(
                    "permission" to permission,
                    "action" to "revoked"
                )
            )
            monitoringRepository.insertLog(log)
        }

        previousPermissions = currentPermissions
    }

    private fun getGrantedPermissions(packageName: String): Set<String> {
        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            val permissions = packageInfo.requestedPermissions ?: return emptySet()
            val flags = packageInfo.requestedPermissionsFlags ?: return emptySet()

            permissions.filterIndexed { index, _ ->
                flags[index] and PackageManager.PERMISSION_GRANTED != 0
            }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun formatPermissionName(permission: String): String {
        return permission
            .removePrefix("android.permission.")
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 3000L
    }
}
