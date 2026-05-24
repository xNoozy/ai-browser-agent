package com.qalab.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalab.launcher.domain.model.MonitoringLog
import com.qalab.launcher.domain.repository.MonitoringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val monitoringRepository: MonitoringRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            monitoringRepository.getRecentLogs(100).collect { logs ->
                _uiState.value = MonitoringUiState(logs = logs)
            }
        }
    }
}

data class MonitoringUiState(
    val logs: List<MonitoringLog> = emptyList(),
    val isLoading: Boolean = false
)
