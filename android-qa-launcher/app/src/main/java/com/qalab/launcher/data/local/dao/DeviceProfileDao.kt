package com.qalab.launcher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qalab.launcher.data.local.entity.DeviceProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: DeviceProfileEntity)

    @Update
    suspend fun update(profile: DeviceProfileEntity)

    @Query("SELECT * FROM device_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<DeviceProfileEntity>>

    @Query("SELECT * FROM device_profiles WHERE id = :id")
    suspend fun getById(id: String): DeviceProfileEntity?

    @Query("SELECT * FROM device_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): DeviceProfileEntity?

    @Query("DELETE FROM device_profiles WHERE id = :id")
    suspend fun delete(id: String)
}
