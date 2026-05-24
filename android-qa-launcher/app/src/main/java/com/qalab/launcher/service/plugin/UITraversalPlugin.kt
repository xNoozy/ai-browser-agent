package com.qalab.launcher.service.plugin

import android.content.Context
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPlugin
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.model.TestResult
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.service.accessibility.QAAccessibilityService
import kotlinx.coroutines.delay

class UITraversalPlugin(private val context: Context) : TestPlugin {

    override val info = TestPluginInfo(
        id = "builtin-ui-traversal",
        name = "UI Traversal",
        version = "1.0.0",
        description = "Systematic UI traversal testing",
        author = "QA Lab",
        pluginType = PluginType.UI_TEST
    )

    private var progressCallback: ((Float, String) -> Unit)? = null
    private var maxDepth = 5
    private var interactionDelay = 1000L

    override suspend fun initialize(config: Map<String, Any>) {
        maxDepth = (config["maxDepth"] as? Number)?.toInt() ?: 5
        interactionDelay = (config["delayMs"] as? Number)?.toLong() ?: 1000L
    }

    override suspend fun execute(session: TestSession): TestResult {
        val logs = mutableListOf<String>()
        var passedTests = 0
        var failedTests = 0
        val startTime = System.currentTimeMillis()
        val visitedScreens = mutableSetOf<String>()

        val service = QAAccessibilityService.instance
        if (service == null) {
            return TestResult(
                pluginId = info.id,
                sessionId = session.id,
                passed = false,
                totalTests = 0,
                passedTests = 0,
                failedTests = 1,
                skippedTests = 0,
                duration = 0,
                logs = listOf("Accessibility service not available")
            )
        }

        // Systematic traversal - visit each clickable element
        val uiNodes = service.getUiHierarchy()
        val clickableNodes = uiNodes.filter { it.isClickable && it.depth <= maxDepth }
        val totalNodes = clickableNodes.size

        logs.add("Found $totalNodes clickable elements to traverse")

        clickableNodes.forEachIndexed { index, node ->
            try {
                val screenId = "${node.className}:${node.viewId ?: node.text ?: index}"
                if (screenId !in visitedScreens) {
                    visitedScreens.add(screenId)

                    val centerX = (node.bounds.left + node.bounds.right) / 2f
                    val centerY = (node.bounds.top + node.bounds.bottom) / 2f
                    service.performTap(centerX, centerY)

                    delay(interactionDelay)

                    logs.add("Visited: ${node.className} - ${node.text ?: node.contentDescription ?: "unnamed"}")
                    passedTests++
                }

                progressCallback?.invoke(
                    (index + 1).toFloat() / totalNodes,
                    "Traversing ${index + 1}/$totalNodes"
                )
            } catch (e: Exception) {
                failedTests++
                logs.add("Failed: ${node.className} - ${e.message}")
            }
        }

        val duration = System.currentTimeMillis() - startTime

        return TestResult(
            pluginId = info.id,
            sessionId = session.id,
            passed = failedTests == 0,
            totalTests = totalNodes,
            passedTests = passedTests,
            failedTests = failedTests,
            skippedTests = totalNodes - passedTests - failedTests,
            duration = duration,
            logs = logs
        )
    }

    override suspend fun cleanup() {}

    override fun onProgress(callback: (Float, String) -> Unit) {
        progressCallback = callback
    }
}
