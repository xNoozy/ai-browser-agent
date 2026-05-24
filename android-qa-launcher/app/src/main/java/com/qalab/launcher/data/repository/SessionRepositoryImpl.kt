package com.qalab.launcher.data.repository

import com.qalab.launcher.data.local.dao.SessionDao
import com.qalab.launcher.data.local.entity.SessionEntity
import com.qalab.launcher.domain.model.SandboxConfig
import com.qalab.launcher.domain.model.SessionStatus
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.domain.repository.SessionRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {

    private val gson = Gson()

    override fun getAllSessions(): Flow<List<TestSession>> {
        return dao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveSessions(): Flow<List<TestSession>> {
        return dao.getActiveSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveSessionsList(): List<TestSession> {
        return dao.getActiveSessionsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): TestSession? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun createSession(session: TestSession) {
        dao.insert(session.toEntity())
    }

    override suspend fun updateSession(session: TestSession) {
        dao.update(session.toEntity())
    }

    override suspend fun updateStatus(id: String, status: SessionStatus) {
        dao.updateStatus(id, status.name)
    }

    override suspend fun incrementCrashCount(id: String) {
        dao.incrementCrashCount(id)
    }

    override suspend fun incrementAnrCount(id: String) {
        dao.incrementAnrCount(id)
    }

    override suspend fun deleteSession(id: String) {
        dao.delete(id)
    }

    private fun SessionEntity.toDomain(): TestSession {
        val config = sandboxConfig?.let {
            gson.fromJson(it, SandboxConfig::class.java)
        } ?: SandboxConfig()

        return TestSession(
            id = id,
            targetPackage = targetPackage,
            targetAppName = targetAppName,
            deviceProfileId = deviceProfileId,
            status = SessionStatus.valueOf(status),
            workProfileId = workProfileId,
            sandboxConfig = config,
            crashCount = crashCount,
            anrCount = anrCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            endedAt = endedAt
        )
    }

    private fun TestSession.toEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            targetPackage = targetPackage,
            targetAppName = targetAppName,
            deviceProfileId = deviceProfileId,
            status = status.name,
            workProfileId = workProfileId,
            sandboxConfig = gson.toJson(sandboxConfig),
            crashCount = crashCount,
            anrCount = anrCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            endedAt = endedAt
        )
    }
}
