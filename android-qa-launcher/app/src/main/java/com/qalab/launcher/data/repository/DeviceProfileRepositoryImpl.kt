package com.qalab.launcher.data.repository

import com.qalab.launcher.data.local.dao.DeviceProfileDao
import com.qalab.launcher.data.local.entity.DeviceProfileEntity
import com.qalab.launcher.domain.model.DeviceProfile
import com.qalab.launcher.domain.model.NetworkType
import com.qalab.launcher.domain.model.Orientation
import com.qalab.launcher.domain.repository.DeviceProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceProfileRepositoryImpl @Inject constructor(
    private val dao: DeviceProfileDao
) : DeviceProfileRepository {

    override fun getAllProfiles(): Flow<List<DeviceProfile>> {
        return dao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): DeviceProfile? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun getDefault(): DeviceProfile? {
        return dao.getDefault()?.toDomain()
    }

    override suspend fun createProfile(profile: DeviceProfile) {
        dao.insert(profile.toEntity())
    }

    override suspend fun updateProfile(profile: DeviceProfile) {
        dao.update(profile.toEntity())
    }

    override suspend fun deleteProfile(id: String) {
        dao.delete(id)
    }

    private fun DeviceProfileEntity.toDomain(): DeviceProfile {
        return DeviceProfile(
            id = id,
            name = name,
            manufacturer = manufacturer,
            model = model,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            densityDpi = densityDpi,
            sdkVersion = sdkVersion,
            locale = locale,
            orientation = Orientation.valueOf(orientation.uppercase()),
            fontScale = fontScale,
            isNightMode = isNightMode,
            networkType = NetworkType.valueOf(networkType.uppercase()),
            isDefault = isDefault,
            createdAt = createdAt
        )
    }

    private fun DeviceProfile.toEntity(): DeviceProfileEntity {
        return DeviceProfileEntity(
            id = id,
            name = name,
            manufacturer = manufacturer,
            model = model,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            densityDpi = densityDpi,
            sdkVersion = sdkVersion,
            locale = locale,
            orientation = orientation.name.lowercase(),
            fontScale = fontScale,
            isNightMode = isNightMode,
            networkType = networkType.name.lowercase(),
            isDefault = isDefault,
            createdAt = createdAt
        )
    }
}
