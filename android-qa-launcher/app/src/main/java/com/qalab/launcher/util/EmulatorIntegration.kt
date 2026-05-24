package com.qalab.launcher.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object EmulatorIntegration {

    data class EmulatorInfo(
        val name: String,
        val serialNumber: String,
        val state: String,
        val apiLevel: Int? = null
    )

    suspend fun listAvailableEmulators(): List<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("emulator", "-list-avds"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val avds = mutableListOf<String>()
            reader.forEachLine { line ->
                if (line.isNotBlank()) avds.add(line.trim())
            }
            process.waitFor()
            avds
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun listRunningDevices(): List<EmulatorInfo> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("adb", "devices", "-l"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val devices = mutableListOf<EmulatorInfo>()

            reader.forEachLine { line ->
                if (line.contains("device") && !line.startsWith("List")) {
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        val serial = parts[0]
                        val state = parts[1]
                        val model = parts.find { it.startsWith("model:") }
                            ?.removePrefix("model:") ?: serial

                        devices.add(
                            EmulatorInfo(
                                name = model,
                                serialNumber = serial,
                                state = state
                            )
                        )
                    }
                }
            }
            process.waitFor()
            devices
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun launchEmulator(avdName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Runtime.getRuntime().exec(
                arrayOf("emulator", "-avd", avdName, "-no-snapshot-load")
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun installApk(serial: String, apkPath: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("adb", "-s", serial, "install", "-r", apkPath)
                )
                val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Result.success(output)
                } else {
                    val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun executeShellCommand(serial: String, command: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("adb", "-s", serial, "shell", command)
                )
                val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
                process.waitFor()
                Result.success(output)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun changeDisplaySize(serial: String, width: Int, height: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec(
                    arrayOf("adb", "-s", serial, "shell", "wm", "size", "${width}x${height}")
                ).waitFor()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun changeDensity(serial: String, dpi: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec(
                    arrayOf("adb", "-s", serial, "shell", "wm", "density", dpi.toString())
                ).waitFor()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun simulateNetworkCondition(
        serial: String,
        speed: String,
        latency: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    "adb", "-s", serial, "shell",
                    "settings", "put", "global", "network_score_config",
                    "speed=$speed,latency=$latency"
                )
            ).waitFor()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
