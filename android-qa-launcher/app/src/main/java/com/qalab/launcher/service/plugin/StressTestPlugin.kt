package com.qalab.launcher.service.plugin

import android.app.ActivityManager
import android.content.Context
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPlugin
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.model.TestResult
import com.qalab.launcher.domain.model.TestSession
import kotlinx.coroutines.delay

class StressTestPlugin(private val context: Context) : TestPlugin {

    override val info = TestPluginInfo(
        id = "builtin-stress-test",
        name = "Stress Test",
        version = "1.0.0",
        description = "Memory and performance stress testing",
        author = "QA Lab",
        pluginType = PluginType.STRESS_TEST
    )

    private var progressCallback: ((Float, String) -> Unit)? = null
    private var durationSeconds = 60
    private var sampleIntervalMs = 2000L

    override suspend fun initialize(config: Map<String, Any>) {
        durationSeconds = (config["durationSeconds"] as? Number)?.toInt() ?: 60
        sampleIntervalMs = (config["sampleIntervalMs"] as? Number)?.toLong() ?: 2000L
    }

    override suspend fun execute(session: TestSession): TestResult {
        val logs = mutableListOf<String>()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (durationSeconds * 1000L)
        var passedTests = 0
        var failedTests = 0
        var sampleCount = 0

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        val initialMemory = getAppMemoryUsage(activityManager, session.targetPackage)
        logs.add("Initial memory usage: ${initialMemory}MB")

        while (System.currentTimeMillis() < endTime) {
            sampleCount++
            val elapsed = System.currentTimeMillis() - startTime
            val progress = elapsed.toFloat() / (durationSeconds * 1000f)

            activityManager.getMemoryInfo(memoryInfo)
            val currentMemory = getAppMemoryUsage(activityManager, session.targetPackage)

            // Check for memory leaks (memory grows continuously)
            val memoryGrowth = currentMemory - initialMemory
            if (memoryGrowth > MEMORY_LEAK_THRESHOLD_MB) {
                failedTests++
                logs.add("⚠ Potential memory leak: +${memoryGrowth}MB at ${elapsed / 1000}s")
            } else {
                passedTests++
            }

            // Check system memory pressure
            if (memoryInfo.lowMemory) {
                failedTests++
                logs.add("⚠ System low memory detected at ${elapsed / 1000}s")
            }

            progressCallback?.invoke(
                progress,
                "Stress testing... ${elapsed / 1000}s/${durationSeconds}s (Memory: ${currentMemory}MB)"
            )

            delay(sampleIntervalMs)
        }

        val finalMemory = getAppMemoryUsage(activityManager, session.targetPackage)
        val totalGrowth = finalMemory - initialMemory
        logs.add("Final memory: ${finalMemory}MB (growth: +${totalGrowth}MB)")
        logs.add("Samples taken: $sampleCount")

        val duration = System.currentTimeMillis() - startTime

        return TestResult(
            pluginId = info.id,
            sessionId = session.id,
            passed = failedTests == 0,
            totalTests = sampleCount,
            passedTests = passedTests,
            failedTests = failedTests,
            skippedTests = 0,
            duration = duration,
            logs = logs
        )
    }

    override suspend fun cleanup() {}

    override fun onProgress(callback: (Float, String) -> Unit) {
        progressCallback = callback
    }

    private fun getAppMemoryUsage(am: ActivityManager, packageName: String): Long {
        val pids = am.runningAppProcesses
            ?.filter { it.processName.startsWith(packageName) }
            ?.map { it.pid }
            ?.toIntArray() ?: return 0

        if (pids.isEmpty()) return 0

        val memInfo = am.getProcessMemoryInfo(pids)
        return memInfo.sumOf { it.totalPss.toLong() } / 1024 // Convert to MB
    }

    companion object {
        private const val MEMORY_LEAK_THRESHOLD_MB = 50
    }
}
