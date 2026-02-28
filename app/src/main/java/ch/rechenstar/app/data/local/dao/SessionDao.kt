package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ch.rechenstar.app.data.local.entity.SessionEntity
import ch.rechenstar.app.data.local.entity.SessionWithRecords

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: String): SessionEntity?

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getWithRecords(sessionId: String): SessionWithRecords?

    @Query("SELECT * FROM sessions WHERE dailyProgressId = :progressId ORDER BY startTime DESC")
    suspend fun getForProgress(progressId: Long): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE dailyProgressId IN (SELECT id FROM daily_progress WHERE userId = :userId) ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentForUser(userId: String, limit: Int = 10): List<SessionEntity>
}
