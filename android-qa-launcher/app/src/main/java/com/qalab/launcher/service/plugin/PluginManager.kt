package com.qalab.launcher.service.plugin

import android.content.Context
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPlugin
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.model.TestResult
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.domain.repository.PluginRepository
import com.qalab.launcher.service.accessibility.QAAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pluginRepository: PluginRepository
) {
    private val loadedPlugins = mutableMapOf<String, TestPlugin>()

    private val _runningPlugin = MutableStateFlow<String?>(null)
    val runningPlugin: StateFlow<String?> = _runningPlugin

    private val _progress = MutableStateFlow(PluginProgress())
    val progress: StateFlow<PluginProgress> = _progress

    fun getInstalledPlugins(): Flow<List<TestPluginInfo>> {
        return pluginRepository.getAllPlugins()
    }

    fun getEnabledPlugins(): Flow<List<TestPluginInfo>> {
        return pluginRepository.getEnabledPlugins()
    }

    suspend fun installBuiltinPlugins() {
        val builtins = listOf(
            TestPluginInfo(
                id = "builtin-monkey-test",
                name = "Monkey Test",
                version = "1.0.0",
                description = "Random UI interaction stress test (similar to Android Monkey)",
                author = "QA Lab",
                pluginType = PluginType.MONKEY_TEST
            ),
            TestPluginInfo(
                id = "builtin-ui-traversal",
                name = "UI Traversal",
                version = "1.0.0",
                description = "Systematic UI traversal and screenshot capture",
                author = "QA Lab",
                pluginType = PluginType.UI_TEST
            ),
            TestPluginInfo(
                id = "builtin-stress-test",
                name = "Stress Test",
                version = "1.0.0",
                description = "Memory and CPU stress testing with leak detection",
                author = "QA Lab",
                pluginType = PluginType.STRESS_TEST
            )
        )

        builtins.forEach { plugin ->
            if (pluginRepository.getById(plugin.id) == null) {
                pluginRepository.installPlugin(plugin)
            }
        }
    }

    suspend fun executePlugin(pluginId: String, session: TestSession): Result<TestResult> {
        val pluginInfo = pluginRepository.getById(pluginId)
            ?: return Result.failure(IllegalArgumentException("Plugin $pluginId not found"))

        if (!pluginInfo.isEnabled) {
            return Result.failure(IllegalStateException("Plugin ${pluginInfo.name} is disabled"))
        }

        _runningPlugin.value = pluginId

        return try {
            val plugin = getOrLoadPlugin(pluginInfo)
            plugin.onProgress { progress, message ->
                _progress.value = PluginProgress(progress, message, pluginId)
            }
            plugin.initialize(parseConfig(pluginInfo.configJson))
            val result = plugin.execute(session)
            plugin.cleanup()

            pluginRepository.updatePlugin(
                pluginInfo.copy(lastRunAt = System.currentTimeMillis())
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _runningPlugin.value = null
            _progress.value = PluginProgress()
        }
    }

    private fun getOrLoadPlugin(info: TestPluginInfo): TestPlugin {
        return loadedPlugins.getOrPut(info.id) {
            when (info.pluginType) {
                PluginType.MONKEY_TEST -> MonkeyTestPlugin(context)
                PluginType.UI_TEST -> UITraversalPlugin(context)
                PluginType.STRESS_TEST -> StressTestPlugin(context)
                PluginType.CUSTOM -> CustomScriptPlugin(context, info.scriptPath)
            }
        }
    }

    private fun parseConfig(json: String?): Map<String, Any> {
        if (json.isNullOrEmpty()) return emptyMap()
        return try {
            com.google.gson.Gson().fromJson(
                json,
                object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

data class PluginProgress(
    val progress: Float = 0f,
    val message: String = "",
    val pluginId: String? = null
)
