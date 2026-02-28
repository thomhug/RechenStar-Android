package ch.rechenstar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ch.rechenstar.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getPreferences(userId: String): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getPreferencesSync(userId: String): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(prefs: UserPreferencesEntity)

    @Update
    suspend fun updatePreferences(prefs: UserPreferencesEntity)
}
