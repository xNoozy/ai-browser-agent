package com.qalab.launcher.service.sandbox

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import com.qalab.launcher.domain.model.SandboxConfig
import com.qalab.launcher.domain.model.SessionStatus
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SandboxManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository
) {
    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val userManager: UserManager =
        context.getSystemService(Context.USER_SERVICE) as UserManager

    private val packageManager: PackageManager = context.packageManager

    private val adminComponent = ComponentName(context, QADeviceAdminReceiver::class.java)

    suspend fun createSandboxSession(session: TestSession): Result<TestSession> {
        return withContext(Dispatchers.IO) {
            try {
                if (!isWorkProfileAvailable()) {
                    return@withContext Result.failure(
                        IllegalStateException("Work profile not available. Device admin must be set up first.")
                    )
                }

                sessionRepository.createSession(session)
                Result.success(session)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun launchAppInSandbox(session: TestSession): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(session.targetPackage)
                    ?: return@withContext Result.failure(
                        IllegalArgumentException("Package ${session.targetPackage} not found")
                    )

                applySandboxConfig(session.sandboxConfig, session.targetPackage)

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)

                sessionRepository.updateStatus(session.id, SessionStatus.RUNNING)
                Result.success(Unit)
            } catch (e: Exception) {
                sessionRepository.updateStatus(session.id, SessionStatus.CRASHED)
                Result.failure(e)
            }
        }
    }

    suspend fun stopSandboxSession(sessionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val session = sessionRepository.getById(sessionId)
                    ?: return@withContext Result.failure(
                        IllegalArgumentException("Session $sessionId not found")
                    )

                if (session.sandboxConfig.clearDataOnStop) {
                    clearAppData(session.targetPackage)
                }

                sessionRepository.updateStatus(sessionId, SessionStatus.STOPPED)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun isWorkProfileAvailable(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent) ||
            userManager.userProfiles.size > 1
    }

    fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }

    fun isProfileOwner(): Boolean {
        return devicePolicyManager.isProfileOwnerApp(context.packageName)
    }

    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0).map { resolveInfo ->
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager),
                versionName = try {
                    packageManager.getPackageInfo(
                        resolveInfo.activityInfo.packageName, 0
                    ).versionName ?: "Unknown"
                } catch (e: Exception) { "Unknown" }
            )
        }.distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .sortedBy { it.appName }
    }

    private fun applySandboxConfig(config: SandboxConfig, packageName: String) {
        if (config.restrictPermissions.isNotEmpty() && isProfileOwner()) {
            config.restrictPermissions.forEach { permission ->
                devicePolicyManager.setPermissionGrantState(
                    adminComponent,
                    packageName,
                    permission,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
                )
            }
        }
    }

    private fun clearAppData(packageName: String) {
        if (isDeviceOwner() || isProfileOwner()) {
            devicePolicyManager.clearApplicationUserData(
                adminComponent,
                packageName,
                context.mainExecutor
            ) { _, success ->
                // Log the result
            }
        }
    }

    fun provisionWorkProfile() {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                adminComponent
            )
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                true
            )
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable,
    val versionName: String
)
