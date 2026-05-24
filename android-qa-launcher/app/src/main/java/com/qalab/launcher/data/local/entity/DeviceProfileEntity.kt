package com.qalab.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_profiles")
data class DeviceProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val densityDpi: Int,
    val sdkVersion: Int,
    val locale: String = "en_US",
    val orientation: String = "portrait", // portrait, landscape
    val fontScale: Float = 1.0f,
    val isNightMode: Boolean = false,
    val networkType: String = "wifi", // wifi, 4g, 3g, 2g, offline
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
