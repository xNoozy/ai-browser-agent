package com.qalab.launcher.data.repository

import com.qalab.launcher.data.local.dao.PluginDao
import com.qalab.launcher.data.local.entity.PluginEntity
import com.qalab.launcher.domain.model.PluginType
import com.qalab.launcher.domain.model.TestPluginInfo
import com.qalab.launcher.domain.repository.PluginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PluginRepositoryImpl @Inject constructor(
    private val dao: PluginDao
) : PluginRepository {

    override fun getAllPlugins(): Flow<List<TestPluginInfo>> {
        return dao.getAllPlugins().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEnabledPlugins(): Flow<List<TestPluginInfo>> {
        return dao.getEnabledPlugins().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getByType(type: PluginType): Flow<List<TestPluginInfo>> {
        return dao.getByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): TestPluginInfo? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun installPlugin(plugin: TestPluginInfo) {
        dao.insert(plugin.toEntity())
    }

    override suspend fun updatePlugin(plugin: TestPluginInfo) {
        dao.update(plugin.toEntity())
    }

    override suspend fun uninstallPlugin(id: String) {
        dao.delete(id)
    }

    private fun PluginEntity.toDomain(): TestPluginInfo {
        return TestPluginInfo(
            id = id,
            name = name,
            version = version,
            description = description,
            author = author,
            pluginType = PluginType.valueOf(pluginType),
            configJson = configJson,
            scriptPath = scriptPath,
            isEnabled = isEnabled,
            lastRunAt = lastRunAt,
            installedAt = installedAt
        )
    }

    private fun TestPluginInfo.toEntity(): PluginEntity {
        return PluginEntity(
            id = id,
            name = name,
            version = version,
            description = description,
            author = author,
            pluginType = pluginType.name,
            configJson = configJson,
            scriptPath = scriptPath,
            isEnabled = isEnabled,
            lastRunAt = lastRunAt,
            installedAt = installedAt
        )
    }
}
