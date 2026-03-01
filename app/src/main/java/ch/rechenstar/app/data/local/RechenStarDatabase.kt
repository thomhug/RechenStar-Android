package ch.rechenstar.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class RechenStarDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_preferences ADD COLUMN fontSize TEXT NOT NULL DEFAULT 'normal'")
                db.execSQL("ALTER TABLE user_preferences ADD COLUMN appearance TEXT NOT NULL DEFAULT 'auto'")
            }
        }
    }

    abstract fun userDao(): UserDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseRecordDao(): ExerciseRecordDao
    abstract fun achievementDao(): AchievementDao
    abstract fun adjustmentLogDao(): AdjustmentLogDao
}
