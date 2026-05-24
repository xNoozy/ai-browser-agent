package com.qalab.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.repository.MonitoringRepository
import com.qalab.launcher.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val monitoringRepository: MonitoringRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                sessionRepository.getActiveSessions(),
                monitoringRepository.getRecentLogs(20)
            ) { sessions, logs ->
                DashboardUiState(
                    activeSessions = sessions.size,
                    totalCrashes = sessions.sumOf { it.crashCount },
                    totalAnrs = sessions.sumOf { it.anrCount },
                    networkIssues = logs.count { it.logType.name == "NETWORK" },
                    recentLogs = logs
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

data class DashboardUiState(
    val activeSessions: Int = 0,
    val totalCrashes: Int = 0,
    val totalAnrs: Int = 0,
    val networkIssues: Int = 0,
    val recentLogs: List<MonitoringLog> = emptyList(),
    val isLoading: Boolean = false
)
