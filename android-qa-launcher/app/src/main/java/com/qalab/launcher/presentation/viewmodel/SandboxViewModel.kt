package com.qalab.launcher.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.domain.repository.SessionRepository
import com.qalab.launcher.service.monitoring.MonitoringService
import com.qalab.launcher.service.sandbox.SandboxManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SandboxViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val sandboxManager: SandboxManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SandboxUiState())
    val uiState: StateFlow<SandboxUiState> = _uiState

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            combine(
                sessionRepository.getActiveSessions(),
                sessionRepository.getAllSessions()
            ) { active, all ->
                SandboxUiState(
                    activeSessions = active,
                    allSessions = all,
                    installedApps = sandboxManager.getInstalledApps()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startSession(sessionId: String) {
        viewModelScope.launch {
            val session = sessionRepository.getById(sessionId) ?: return@launch
            sandboxManager.launchAppInSandbox(session)

            // Start monitoring service
            val intent = MonitoringService.createIntent(
                context, sessionId, session.targetPackage
            )
            context.startForegroundService(intent)
        }
    }

    fun stopSession(sessionId: String) {
        viewModelScope.launch {
            sandboxManager.stopSandboxSession(sessionId)
            context.stopService(Intent(context, MonitoringService::class.java))
        }
    }

    fun showCreateSessionDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun createSession(targetPackage: String, targetAppName: String) {
        viewModelScope.launch {
            val session = TestSession(
                targetPackage = targetPackage,
                targetAppName = targetAppName
            )
            sandboxManager.createSandboxSession(session)
            _uiState.value = _uiState.value.copy(showCreateDialog = false)
        }
    }
}

data class SandboxUiState(
    val activeSessions: List<TestSession> = emptyList(),
    val allSessions: List<TestSession> = emptyList(),
    val installedApps: List<com.qalab.launcher.service.sandbox.AppInfo> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = false
)
