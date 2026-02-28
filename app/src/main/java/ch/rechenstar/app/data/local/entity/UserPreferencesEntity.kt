package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_preferences",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserPreferencesEntity(
    @PrimaryKey val userId: String,
    // Gameplay
    val difficultyLevel: Int = 2,
    val adaptiveDifficulty: Boolean = true,
    val sessionLength: Int = 10,
    val dailyGoal: Int = 20,
    val gapFillEnabled: Boolean = true,
    val hideSkipButton: Boolean = false,
    val autoShowAnswerSeconds: Int = 0,
    // Audio & Haptics
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    // Visual
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val largerText: Boolean = false,
    val colorBlindMode: String = "none",
    // Categories
    val enabledCategoriesRaw: String = "addition_10,subtraction_10",
    // Parental
    val timeLimitMinutes: Int = 0,
    val timeLimitEnabled: Boolean = false,
    val breakReminder: Boolean = true,
    val breakIntervalSeconds: Int = 900
)
