package ch.rechenstar.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.rechenstar.app.data.local.dao.AchievementDao
import ch.rechenstar.app.data.local.dao.AdjustmentLogDao
import ch.rechenstar.app.data.local.dao.DailyProgressDao
import ch.rechenstar.app.data.local.dao.ExerciseRecordDao
import ch.rechenstar.app.data.local.dao.SessionDao
import ch.rechenstar.app.data.local.dao.UserDao
import ch.rechenstar.app.data.local.dao.UserPreferencesDao
import ch.rechenstar.app.data.local.entity.AchievementEntity
import ch.rechenstar.app.data.local.entity.AdjustmentLogEntity
import ch.rechenstar.app.data.local.entity.DailyProgressEntity
import ch.rechenstar.app.data.local.entity.ExerciseRecordEntity
import ch.rechenstar.app.data.local.entity.SessionEntity
import ch.rechenstar.app.data.local.entity.UserEntity
import ch.rechenstar.app.data.local.entity.UserPreferencesEntity

@Database(
    entities = [
        UserEntity::class,
        UserPreferencesEntity::class,
        DailyProgressEntity::class,
        SessionEntity::class,
        ExerciseRecordEntity::class,
        AchievementEntity::class,
        AdjustmentLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RechenStarDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseRecordDao(): ExerciseRecordDao
    abstract fun achievementDao(): AchievementDao
    abstract fun adjustmentLogDao(): AdjustmentLogDao
}
