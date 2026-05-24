package com.qalab.launcher.domain.repository

import com.qalab.launcher.domain.model.DeviceProfile
import kotlinx.coroutines.flow.Flow

interface DeviceProfileRepository {
    fun getAllProfiles(): Flow<List<DeviceProfile>>
    suspend fun getById(id: String): DeviceProfile?
    suspend fun getDefault(): DeviceProfile?
    suspend fun createProfile(profile: DeviceProfile)
    suspend fun updateProfile(profile: DeviceProfile)
    suspend fun deleteProfile(id: String)
}
