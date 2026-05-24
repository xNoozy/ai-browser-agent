package com.qalab.launcher.domain.usecase

import com.qalab.launcher.domain.model.SandboxConfig
import com.qalab.launcher.domain.model.SessionStatus
import com.qalab.launcher.domain.model.TestSession
import com.qalab.launcher.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        targetPackage: String,
        targetAppName: String,
        deviceProfileId: String? = null,
        sandboxConfig: SandboxConfig = SandboxConfig()
    ): TestSession {
        val session = TestSession(
            targetPackage = targetPackage,
            targetAppName = targetAppName,
            deviceProfileId = deviceProfileId,
            sandboxConfig = sandboxConfig
        )
        repository.createSession(session)
        return session
    }
}

class StartSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: String) {
        repository.updateStatus(sessionId, SessionStatus.RUNNING)
    }
}

class StopSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(sessionId: String) {
        repository.updateStatus(sessionId, SessionStatus.STOPPED)
    }
}

class GetActiveSessionsUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<List<TestSession>> {
        return repository.getActiveSessions()
    }
}

class GetAllSessionsUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<List<TestSession>> {
        return repository.getAllSessions()
    }
}
