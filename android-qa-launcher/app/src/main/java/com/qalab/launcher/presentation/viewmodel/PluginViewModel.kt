package com.qalab.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.repository.PluginRepository
import com.qalab.launcher.domain.repository.SessionRepository
import com.qalab.launcher.service.plugin.PluginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PluginViewModel @Inject constructor(
    private val pluginRepository: PluginRepository,
    private val pluginManager: PluginManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PluginUiState())
    val uiState: StateFlow<PluginUiState> = _uiState

    init {
        loadPlugins()
        installBuiltins()
        observeProgress()
    }

    private fun loadPlugins() {
        viewModelScope.launch {
            pluginRepository.getAllPlugins().collect { plugins ->
                _uiState.value = _uiState.value.copy(plugins = plugins)
            }
        }
    }

    private fun installBuiltins() {
        viewModelScope.launch {
            pluginManager.installBuiltinPlugins()
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            pluginManager.progress.collect { progress ->
                _uiState.value = _uiState.value.copy(
                    progress = progress.progress,
                    progressMessage = progress.message,
                    runningPluginId = progress.pluginId,
                    isRunning = progress.pluginId != null
                )
            }
        }
    }

    fun togglePlugin(plugin: TestPluginInfo) {
        viewModelScope.launch {
            pluginRepository.updatePlugin(plugin.copy(isEnabled = !plugin.isEnabled))
        }
    }

    fun runPlugin(pluginId: String) {
        viewModelScope.launch {
            val activeSessions = sessionRepository.getActiveSessionsList()
            val session = activeSessions.firstOrNull()
            if (session != null) {
                pluginManager.executePlugin(pluginId, session)
            }
        }
    }
}

data class PluginUiState(
    val plugins: List<TestPluginInfo> = emptyList(),
    val isRunning: Boolean = false,
    val runningPluginId: String? = null,
    val progress: Float = 0f,
    val progressMessage: String = ""
)
