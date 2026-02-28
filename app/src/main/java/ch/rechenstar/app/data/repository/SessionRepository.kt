package ch.rechenstar.app.data.repository

import ch.rechenstar.app.data.local.dao.ExerciseRecordDao
import ch.rechenstar.app.data.local.dao.SessionDao
import ch.rechenstar.app.data.local.entity.ExerciseRecordEntity
import ch.rechenstar.app.data.local.entity.SessionEntity
import ch.rechenstar.app.data.local.entity.SessionWithRecords
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val exerciseRecordDao: ExerciseRecordDao
) {
    suspend fun insertSession(session: SessionEntity) = sessionDao.insert(session)

    suspend fun updateSession(session: SessionEntity) = sessionDao.update(session)

    suspend fun getSessionWithRecords(sessionId: String): SessionWithRecords? =
        sessionDao.getWithRecords(sessionId)

    suspend fun insertRecord(record: ExerciseRecordEntity) = exerciseRecordDao.insert(record)

    suspend fun insertRecords(records: List<ExerciseRecordEntity>) =
        exerciseRecordDao.insertAll(records)

    suspend fun getRecentRecords(userId: String, since: Long): List<ExerciseRecordEntity> =
        exerciseRecordDao.getRecentForUser(userId, since)

    suspend fun getAllRecords(userId: String): List<ExerciseRecordEntity> =
        exerciseRecordDao.getAllForUser(userId)

    suspend fun getRecentSessions(userId: String, limit: Int = 10): List<SessionEntity> =
        sessionDao.getRecentForUser(userId, limit)
}
