package com.qalab.launcher.domain.repository

import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPluginInfo
import kotlinx.coroutines.flow.Flow

interface PluginRepository {
    fun getAllPlugins(): Flow<List<TestPluginInfo>>
    fun getEnabledPlugins(): Flow<List<TestPluginInfo>>
    fun getByType(type: PluginType): Flow<List<TestPluginInfo>>
    suspend fun getById(id: String): TestPluginInfo?
    suspend fun installPlugin(plugin: TestPluginInfo)
    suspend fun updatePlugin(plugin: TestPluginInfo)
    suspend fun uninstallPlugin(id: String)
}
