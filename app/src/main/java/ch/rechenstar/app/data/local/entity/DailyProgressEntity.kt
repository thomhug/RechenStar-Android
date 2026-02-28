package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_progress",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("date")]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: Long,
    val exercisesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalTime: Double = 0.0,
    val sessionsCount: Int = 0
)
