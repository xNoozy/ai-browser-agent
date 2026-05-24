package com.qalab.launcher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qalab.launcher.data.local.entity.MonitoringLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoringLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MonitoringLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<MonitoringLogEntity>)

    @Query("SELECT * FROM monitoring_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getLogsBySession(sessionId: String): Flow<List<MonitoringLogEntity>>

    @Query("SELECT * FROM monitoring_logs WHERE sessionId = :sessionId AND logType = :type ORDER BY timestamp DESC")
    fun getLogsByType(sessionId: String, type: String): Flow<List<MonitoringLogEntity>>

    @Query("SELECT * FROM monitoring_logs WHERE severity = 'CRITICAL' OR severity = 'ERROR' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentErrors(limit: Int = 50): Flow<List<MonitoringLogEntity>>

    @Query("SELECT * FROM monitoring_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<MonitoringLogEntity>>

    @Query("SELECT COUNT(*) FROM monitoring_logs WHERE sessionId = :sessionId AND logType = :type")
    suspend fun getCountByType(sessionId: String, type: String): Int

    @Query("DELETE FROM monitoring_logs WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    @Query("DELETE FROM monitoring_logs WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)
}
