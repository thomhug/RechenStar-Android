package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_records",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId"), Index("category"), Index("date")]
)
data class ExerciseRecordEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseSignature: String = "",
    val operationType: String = "",
    val category: String = "",
    val firstNumber: Int = 0,
    val secondNumber: Int = 0,
    val isCorrect: Boolean = false,
    val timeSpent: Double = 0.0,
    val attempts: Int = 1,
    val wasSkipped: Boolean = false,
    val difficulty: Int = 2,
    val date: Long = System.currentTimeMillis()
)
