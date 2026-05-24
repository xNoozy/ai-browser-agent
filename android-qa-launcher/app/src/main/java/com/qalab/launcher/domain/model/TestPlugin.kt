package com.qalab.launcher.domain.model

import java.util.UUID

data class TestPluginInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val pluginType: PluginType,
    val configJson: String? = null,
    val scriptPath: String? = null,
    val isEnabled: Boolean = true,
    val lastRunAt: Long? = null,
    val installedAt: Long = System.currentTimeMillis()
)

enum class PluginType {
    UI_TEST, STRESS_TEST, MONKEY_TEST, CUSTOM
}

data class TestResult(
    val pluginId: String,
    val sessionId: String,
    val passed: Boolean,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val skippedTests: Int,
    val duration: Long,
    val logs: List<String>,
    val screenshots: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

interface TestPlugin {
    val info: TestPluginInfo
    suspend fun initialize(config: Map<String, Any>)
    suspend fun execute(session: TestSession): TestResult
    suspend fun cleanup()
    fun onProgress(callback: (Float, String) -> Unit)
}
