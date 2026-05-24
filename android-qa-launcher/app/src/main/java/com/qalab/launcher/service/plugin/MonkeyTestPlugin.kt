package com.qalab.launcher.service.plugin

import android.content.Context
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPlugin
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.model.TestResult
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.service.accessibility.QAAccessibilityService
import kotlinx.coroutines.delay
import kotlin.random.Random

class MonkeyTestPlugin(private val context: Context) : TestPlugin {

    override val info = TestPluginInfo(
        id = "builtin-monkey-test",
        name = "Monkey Test",
        version = "1.0.0",
        description = "Random UI interaction stress test",
        author = "QA Lab",
        pluginType = PluginType.MONKEY_TEST
    )

    private var progressCallback: ((Float, String) -> Unit)? = null
    private var eventCount = DEFAULT_EVENT_COUNT
    private var delayBetweenEvents = DEFAULT_DELAY_MS

    override suspend fun initialize(config: Map<String, Any>) {
        eventCount = (config["eventCount"] as? Number)?.toInt() ?: DEFAULT_EVENT_COUNT
        delayBetweenEvents = (config["delayMs"] as? Number)?.toLong() ?: DEFAULT_DELAY_MS
    }

    override suspend fun execute(session: TestSession): TestResult {
        val logs = mutableListOf<String>()
        var passedTests = 0
        var failedTests = 0
        val startTime = System.currentTimeMillis()

        val service = QAAccessibilityService.instance
        if (service == null) {
            return TestResult(
                pluginId = info.id,
                sessionId = session.id,
                passed = false,
                totalTests = eventCount,
                passedTests = 0,
                failedTests = 1,
                skippedTests = eventCount - 1,
                duration = 0,
                logs = listOf("Accessibility service not available. Please enable it in Settings.")
            )
        }

        for (i in 1..eventCount) {
            try {
                val action = randomAction(service)
                logs.add("Event $i: $action")
                passedTests++
                progressCallback?.invoke(i.toFloat() / eventCount, "Event $i/$eventCount: $action")
            } catch (e: Exception) {
                failedTests++
                logs.add("Event $i: FAILED - ${e.message}")
            }
            delay(delayBetweenEvents)
        }

        val duration = System.currentTimeMillis() - startTime

        return TestResult(
            pluginId = info.id,
            sessionId = session.id,
            passed = failedTests == 0,
            totalTests = eventCount,
            passedTests = passedTests,
            failedTests = failedTests,
            skippedTests = 0,
            duration = duration,
            logs = logs
        )
    }

    override suspend fun cleanup() {
        // No cleanup needed
    }

    override fun onProgress(callback: (Float, String) -> Unit) {
        progressCallback = callback
    }

    private fun randomAction(service: QAAccessibilityService): String {
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

        return when (Random.nextInt(5)) {
            0 -> {
                val x = Random.nextFloat() * screenWidth
                val y = Random.nextFloat() * screenHeight
                service.performTap(x, y)
                "Tap at (${"%.0f".format(x)}, ${"%.0f".format(y)})"
            }
            1 -> {
                val startX = Random.nextFloat() * screenWidth
                val startY = Random.nextFloat() * screenHeight
                val endX = Random.nextFloat() * screenWidth
                val endY = Random.nextFloat() * screenHeight
                service.performSwipe(startX, startY, endX, endY, 300)
                "Swipe from (${"%.0f".format(startX)}, ${"%.0f".format(startY)}) to (${"%.0f".format(endX)}, ${"%.0f".format(endY)})"
            }
            2 -> {
                val nodes = service.getUiHierarchy().filter { it.isClickable }
                if (nodes.isNotEmpty()) {
                    val node = nodes.random()
                    val centerX = (node.bounds.left + node.bounds.right) / 2f
                    val centerY = (node.bounds.top + node.bounds.bottom) / 2f
                    service.performTap(centerX, centerY)
                    "Click on ${node.className}(${node.text ?: node.contentDescription ?: ""})"
                } else {
                    "No clickable nodes found"
                }
            }
            3 -> {
                val nodes = service.getUiHierarchy().filter { it.isScrollable }
                if (nodes.isNotEmpty()) {
                    "Scroll attempt on scrollable view"
                } else {
                    val x = screenWidth / 2
                    service.performSwipe(x, screenHeight * 0.7f, x, screenHeight * 0.3f, 500)
                    "Scroll gesture"
                }
            }
            else -> {
                service.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                "Back button"
            }
        }
    }

    companion object {
        private const val DEFAULT_EVENT_COUNT = 500
        private const val DEFAULT_DELAY_MS = 300L
    }
}
