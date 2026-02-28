package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rechenstar.app.data.local.entity.ExerciseRecordEntity

@Dao
interface ExerciseRecordDao {

    @Query("SELECT * FROM exercise_records WHERE sessionId = :sessionId")
    suspend fun getForSession(sessionId: String): List<ExerciseRecordEntity>

    @Query("""
        SELECT * FROM exercise_records
        WHERE sessionId IN (
            SELECT id FROM sessions
            WHERE dailyProgressId IN (
                SELECT id FROM daily_progress WHERE userId = :userId
            )
        )
        AND date >= :since
        ORDER BY date DESC
    """)
    suspend fun getRecentForUser(userId: String, since: Long): List<ExerciseRecordEntity>

    @Query("""
        SELECT * FROM exercise_records
        WHERE sessionId IN (
            SELECT id FROM sessions
            WHERE dailyProgressId IN (
                SELECT id FROM daily_progress WHERE userId = :userId
            )
        )
        ORDER BY date DESC
    """)
    suspend fun getAllForUser(userId: String): List<ExerciseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ExerciseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ExerciseRecordEntity>)
}
