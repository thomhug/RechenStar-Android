package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ch.rechenstar.app.data.local.entity.UserEntity
import ch.rechenstar.app.data.local.entity.UserWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY lastActiveAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithRelations(userId: String): UserWithRelations?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("UPDATE users SET totalExercises = :total, totalStars = :stars WHERE id = :userId")
    suspend fun updateStats(userId: String, total: Int, stars: Int)

    @Query("UPDATE users SET currentStreak = :current, longestStreak = :longest WHERE id = :userId")
    suspend fun updateStreak(userId: String, current: Int, longest: Int)

    @Query("UPDATE users SET lastActiveAt = :timestamp WHERE id = :userId")
    suspend fun updateLastActive(userId: String, timestamp: Long)
}
