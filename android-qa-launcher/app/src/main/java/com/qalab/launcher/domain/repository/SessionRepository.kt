package com.qalab.launcher.domain.repository

import com.qalab.launcher.domain.model.SessionStatus
import com.qalab.launcher.domain.model.TestSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<TestSession>>
    fun getActiveSessions(): Flow<List<TestSession>>
    suspend fun getActiveSessionsList(): List<TestSession>
    suspend fun getById(id: String): TestSession?
    suspend fun createSession(session: TestSession)
    suspend fun updateSession(session: TestSession)
    suspend fun updateStatus(id: String, status: SessionStatus)
    suspend fun incrementCrashCount(id: String)
    suspend fun incrementAnrCount(id: String)
    suspend fun deleteSession(id: String)
}
