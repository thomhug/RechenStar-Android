package ch.rechenstar.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class AchievementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val typeRawValue: String = "",
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val target: Int = 1
)
