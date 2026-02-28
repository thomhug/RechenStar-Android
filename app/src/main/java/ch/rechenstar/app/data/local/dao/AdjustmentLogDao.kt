package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rechenstar.app.data.local.entity.AdjustmentLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdjustmentLogDao {

    @Query("SELECT * FROM adjustment_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllForUser(userId: String): Flow<List<AdjustmentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AdjustmentLogEntity)
}
