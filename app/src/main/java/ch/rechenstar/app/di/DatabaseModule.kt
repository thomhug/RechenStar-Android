package ch.rechenstar.app.di

import android.content.Context
import androidx.room.Room
import ch.rechenstar.app.data.local.RechenStarDatabase
import ch.rechenstar.app.data.local.dao.AchievementDao
import ch.rechenstar.app.data.local.dao.AdjustmentLogDao
import ch.rechenstar.app.data.local.dao.DailyProgressDao
import ch.rechenstar.app.data.local.dao.ExerciseRecordDao
import ch.rechenstar.app.data.local.dao.SessionDao
import ch.rechenstar.app.data.local.dao.UserDao
import ch.rechenstar.app.data.local.dao.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RechenStarDatabase =
        Room.databaseBuilder(
            context,
            RechenStarDatabase::class.java,
            "rechenstar.db"
        ).build()

    @Provides fun provideUserDao(db: RechenStarDatabase): UserDao = db.userDao()
    @Provides fun provideUserPreferencesDao(db: RechenStarDatabase): UserPreferencesDao = db.userPreferencesDao()
    @Provides fun provideDailyProgressDao(db: RechenStarDatabase): DailyProgressDao = db.dailyProgressDao()
    @Provides fun provideSessionDao(db: RechenStarDatabase): SessionDao = db.sessionDao()
    @Provides fun provideExerciseRecordDao(db: RechenStarDatabase): ExerciseRecordDao = db.exerciseRecordDao()
    @Provides fun provideAchievementDao(db: RechenStarDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideAdjustmentLogDao(db: RechenStarDatabase): AdjustmentLogDao = db.adjustmentLogDao()
}
