package com.qalab.launcher.di

import android.content.Context
import androidx.room.Room
import com.qalab.launcher.data.local.database.QALabDatabase
import com.qalab.launcher.data.local.dao.MonitoringLogDao
import com.qalab.launcher.data.local.dao.SessionDao
import com.qalab.launcher.data.local.dao.DeviceProfileDao
import com.qalab.launcher.data.local.dao.PluginDao
import com.qalab.launcher.data.repository.MonitoringRepositoryImpl
import com.qalab.launcher.data.repository.SessionRepositoryImpl
import com.qalab.launcher.data.repository.DeviceProfileRepositoryImpl
import com.qalab.launcher.data.repository.PluginRepositoryImpl
import com.qalab.launcher.domain.repository.MonitoringRepository
import com.qalab.launcher.domain.repository.SessionRepository
import com.qalab.launcher.domain.repository.DeviceProfileRepository
import com.qalab.launcher.domain.repository.PluginRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QALabDatabase {
        return Room.databaseBuilder(
            context,
            QALabDatabase::class.java,
            "qalab_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMonitoringLogDao(database: QALabDatabase): MonitoringLogDao {
        return database.monitoringLogDao()
    }

    @Provides
    fun provideSessionDao(database: QALabDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideDeviceProfileDao(database: QALabDatabase): DeviceProfileDao {
        return database.deviceProfileDao()
    }

    @Provides
    fun providePluginDao(database: QALabDatabase): PluginDao {
        return database.pluginDao()
    }

    @Provides
    @Singleton
    fun provideMonitoringRepository(dao: MonitoringLogDao): MonitoringRepository {
        return MonitoringRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(dao: SessionDao): SessionRepository {
        return SessionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideDeviceProfileRepository(dao: DeviceProfileDao): DeviceProfileRepository {
        return DeviceProfileRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providePluginRepository(dao: PluginDao): PluginRepository {
        return PluginRepositoryImpl(dao)
    }
}
