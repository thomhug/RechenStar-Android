package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = DailyProgressEntity::class,
        parentColumns = ["id"],
        childColumns = ["dailyProgressId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dailyProgressId")]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val dailyProgressId: Long,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    val sessionGoal: Int = 10,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val starsEarned: Int = 0,
    val additionCorrect: Int = 0,
    val additionTotal: Int = 0,
    val subtractionCorrect: Int = 0,
    val subtractionTotal: Int = 0
)
