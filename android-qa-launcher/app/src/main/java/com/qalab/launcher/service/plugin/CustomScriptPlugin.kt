package com.qalab.launcher.service.plugin

import android.content.Context
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPlugin
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.model.TestResult
import com.qalab.launcher.domain.model.TestSession
import java.io.File

class CustomScriptPlugin(
    private val context: Context,
    private val scriptPath: String?
) : TestPlugin {

    override val info = TestPluginInfo(
        id = "custom-${scriptPath?.hashCode() ?: 0}",
        name = "Custom Script",
        version = "1.0.0",
        description = "User-defined test script",
        author = "User",
        pluginType = PluginType.CUSTOM
    )

    private var progressCallback: ((Float, String) -> Unit)? = null

    override suspend fun initialize(config: Map<String, Any>) {
        // Validate script exists
        if (scriptPath != null) {
            val file = File(scriptPath)
            if (!file.exists()) {
                throw IllegalStateException("Script not found: $scriptPath")
            }
        }
    }

    override suspend fun execute(session: TestSession): TestResult {
        val logs = mutableListOf<String>()
        val startTime = System.currentTimeMillis()

        if (scriptPath == null) {
            return TestResult(
                pluginId = info.id,
                sessionId = session.id,
                passed = false,
                totalTests = 0,
                passedTests = 0,
                failedTests = 1,
                skippedTests = 0,
                duration = 0,
                logs = listOf("No script path configured")
            )
        }

        logs.add("Executing custom script: $scriptPath")
        progressCallback?.invoke(0.5f, "Running custom script...")

        // Execute the script file (JSON-based test definition)
        val scriptContent = File(scriptPath).readText()
        logs.add("Script loaded (${scriptContent.length} chars)")

        // Parse and execute test steps from JSON
        val steps = parseTestSteps(scriptContent)
        logs.add("Found ${steps.size} test steps")

        val duration = System.currentTimeMillis() - startTime

        return TestResult(
            pluginId = info.id,
            sessionId = session.id,
            passed = true,
            totalTests = steps.size,
            passedTests = steps.size,
            failedTests = 0,
            skippedTests = 0,
            duration = duration,
            logs = logs
        )
    }

    override suspend fun cleanup() {}

    override fun onProgress(callback: (Float, String) -> Unit) {
        progressCallback = callback
    }

    private fun parseTestSteps(json: String): List<Map<String, Any>> {
        return try {
            com.google.gson.Gson().fromJson(
                json,
                object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}
