package ch.rechenstar.app.data.repository

import ch.rechenstar.app.data.local.dao.DailyProgressDao
import ch.rechenstar.app.data.local.entity.DailyProgressEntity
import ch.rechenstar.app.data.local.entity.DailyProgressWithSessions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val dailyProgressDao: DailyProgressDao
) {
    fun getAllForUser(userId: String): Flow<List<DailyProgressEntity>> =
        dailyProgressDao.getAllForUser(userId)

    suspend fun getForDate(userId: String, date: Long): DailyProgressEntity? =
        dailyProgressDao.getForDate(userId, date)

    suspend fun getOrCreateForDate(userId: String, date: Long): DailyProgressEntity {
        val existing = dailyProgressDao.getForDate(userId, date)
        if (existing != null) return existing

        val newProgress = DailyProgressEntity(userId = userId, date = date)
        val id = dailyProgressDao.insert(newProgress)
        return newProgress.copy(id = id)
    }

    suspend fun updateProgress(progress: DailyProgressEntity) =
        dailyProgressDao.update(progress)

    suspend fun getWithSessions(userId: String, date: Long): DailyProgressWithSessions? =
        dailyProgressDao.getWithSessions(userId, date)

    suspend fun getRecent(userId: String, since: Long): List<DailyProgressEntity> =
        dailyProgressDao.getRecent(userId, since)

    suspend fun getLast7Days(userId: String): List<DailyProgressEntity> =
        dailyProgressDao.getLast7Days(userId)

    suspend fun getLast7DaysSync(userId: String): List<DailyProgressEntity> =
        dailyProgressDao.getLast7Days(userId)

    suspend fun getForDateSync(userId: String, date: LocalDate): DailyProgressEntity? {
        val epochMilli = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dailyProgressDao.getForDate(userId, epochMilli)
    }
}
