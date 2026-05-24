package com.qalab.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val pluginType: String, // UI_TEST, STRESS_TEST, MONKEY_TEST, CUSTOM
    val configJson: String? = null,
    val scriptPath: String? = null,
    val isEnabled: Boolean = true,
    val lastRunAt: Long? = null,
    val installedAt: Long = System.currentTimeMillis()
)
