package com.qalab.launcher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qalab.launcher.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE status = 'RUNNING'")
    fun getActiveSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE status = 'RUNNING'")
    suspend fun getActiveSessionsList(): List<SessionEntity>

    @Query("UPDATE sessions SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sessions SET crashCount = crashCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementCrashCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sessions SET anrCount = anrCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementAnrCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String)
}
