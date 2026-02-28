package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "adjustment_logs",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class AdjustmentLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String = ""
)
