package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ch.rechenstar.app.data.local.entity.DailyProgressEntity
import ch.rechenstar.app.data.local.entity.DailyProgressWithSessions
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProgressDao {

    @Query("SELECT * FROM daily_progress WHERE userId = :userId ORDER BY date DESC")
    fun getAllForUser(userId: String): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getForDate(userId: String, date: Long): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND date >= :since ORDER BY date DESC")
    suspend fun getRecent(userId: String, since: Long): List<DailyProgressEntity>

    @Transaction
    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getWithSessions(userId: String, date: Long): DailyProgressWithSessions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: DailyProgressEntity): Long

    @Update
    suspend fun update(progress: DailyProgressEntity)

    @Query("SELECT * FROM daily_progress WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    suspend fun getLast7Days(userId: String): List<DailyProgressEntity>
}
