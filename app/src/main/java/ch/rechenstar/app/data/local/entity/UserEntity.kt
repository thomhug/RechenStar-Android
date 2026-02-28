package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String = "",
    val avatarCharacter: String = "star",
    val avatarColor: String = "#4A90E2",
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalExercises: Int = 0,
    val totalStars: Int = 0
)
