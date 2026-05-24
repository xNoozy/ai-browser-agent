package com.qalab.launcher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qalab.launcher.data.local.entity.PluginEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PluginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plugin: PluginEntity)

    @Update
    suspend fun update(plugin: PluginEntity)

    @Query("SELECT * FROM plugins ORDER BY name ASC")
    fun getAllPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE isEnabled = 1")
    fun getEnabledPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE id = :id")
    suspend fun getById(id: String): PluginEntity?

    @Query("SELECT * FROM plugins WHERE pluginType = :type")
    fun getByType(type: String): Flow<List<PluginEntity>>

    @Query("DELETE FROM plugins WHERE id = :id")
    suspend fun delete(id: String)
}
