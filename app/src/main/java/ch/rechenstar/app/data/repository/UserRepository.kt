package ch.rechenstar.app.data.repository

import ch.rechenstar.app.data.local.dao.AchievementDao
import ch.rechenstar.app.data.local.dao.AdjustmentLogDao
import ch.rechenstar.app.data.local.dao.ExerciseRecordDao
import ch.rechenstar.app.data.local.dao.UserDao
import ch.rechenstar.app.data.local.dao.UserPreferencesDao
import ch.rechenstar.app.data.local.entity.AchievementEntity
import ch.rechenstar.app.data.local.entity.AdjustmentLogEntity
import ch.rechenstar.app.data.local.entity.UserEntity
import ch.rechenstar.app.data.local.entity.UserPreferencesEntity
import ch.rechenstar.app.data.local.entity.UserWithRelations
import ch.rechenstar.app.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val preferencesDao: UserPreferencesDao,
    private val achievementDao: AchievementDao,
    private val adjustmentLogDao: AdjustmentLogDao,
    private val exerciseRecordDao: ExerciseRecordDao
) {
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserById(userId: String): UserEntity? = userDao.getUserById(userId)

    suspend fun getUserWithRelations(userId: String): UserWithRelations? =
        userDao.getUserWithRelations(userId)

    suspend fun createUser(name: String, avatarCharacter: String = "star"): UserEntity {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            avatarCharacter = avatarCharacter
        )
        userDao.insertUser(user)

        // Create default preferences
        preferencesDao.insertPreferences(UserPreferencesEntity(userId = user.id))

        // Create all achievement entries
        val achievements = AchievementType.entries.map { type ->
            AchievementEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                typeRawValue = type.rawValue,
                target = type.defaultTarget
            )
        }
        achievementDao.insertAll(achievements)

        return user
    }

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(userId: String) = userDao.deleteUser(userId)

    suspend fun updateStats(userId: String, totalExercises: Int, totalStars: Int) =
        userDao.updateStats(userId, totalExercises, totalStars)

    suspend fun updateStreak(userId: String, current: Int, longest: Int) =
        userDao.updateStreak(userId, current, longest)

    suspend fun updateLastActive(userId: String) =
        userDao.updateLastActive(userId, System.currentTimeMillis())

    fun getPreferences(userId: String): Flow<UserPreferencesEntity?> =
        preferencesDao.getPreferences(userId)

    suspend fun getPreferencesSync(userId: String): UserPreferencesEntity? =
        preferencesDao.getPreferencesSync(userId)

    suspend fun updatePreferences(prefs: UserPreferencesEntity) =
        preferencesDao.updatePreferences(prefs)

    fun getAchievements(userId: String): Flow<List<AchievementEntity>> =
        achievementDao.getAllForUser(userId)

    suspend fun getAchievementsSync(userId: String): List<AchievementEntity> =
        achievementDao.getAllForUserSync(userId)

    suspend fun updateAchievement(achievement: AchievementEntity) =
        achievementDao.update(achievement)

    suspend fun getAchievementByType(userId: String, type: String): AchievementEntity? =
        achievementDao.getByType(userId, type)

    fun getAdjustmentLogs(userId: String): Flow<List<AdjustmentLogEntity>> =
        adjustmentLogDao.getAllForUser(userId)

    suspend fun getAdjustmentLogsSync(userId: String): List<AdjustmentLogEntity> =
        adjustmentLogDao.getAllForUserSync(userId)

    suspend fun addAdjustmentLog(userId: String, summary: String) =
        adjustmentLogDao.insert(
            AdjustmentLogEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                summary = summary
            )
        )

    suspend fun getRecentExerciseRecords(userId: String, days: Int): List<ch.rechenstar.app.data.local.entity.ExerciseRecordEntity> {
        val since = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        return exerciseRecordDao.getRecentForUser(userId, since)
    }
}
