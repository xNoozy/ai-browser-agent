package com.qalab.launcher.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qalab.launcher.data.local.dao.DeviceProfileDao
import com.qalab.launcher.data.local.dao.MonitoringLogDao
import com.qalab.launcher.data.local.dao.PluginDao
import com.qalab.launcher.data.local.dao.SessionDao
import com.qalab.launcher.data.local.entity.DeviceProfileEntity
import com.qalab.launcher.data.local.entity.MonitoringLogEntity
import com.qalab.launcher.data.local.entity.PluginEntity
import com.qalab.launcher.data.local.entity.SessionEntity

@Database(
    entities = [
        MonitoringLogEntity::class,
        SessionEntity::class,
        DeviceProfileEntity::class,
        PluginEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class QALabDatabase : RoomDatabase() {
    abstract fun monitoringLogDao(): MonitoringLogDao
    abstract fun sessionDao(): SessionDao
    abstract fun deviceProfileDao(): DeviceProfileDao
    abstract fun pluginDao(): PluginDao
}
