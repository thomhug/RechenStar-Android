package ch.rechenstar.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithRelations(
    @Embedded val user: UserEntity,

    @Relation(parentColumn = "id", entityColumn = "userId")
    val preferences: UserPreferencesEntity?,

    @Relation(parentColumn = "id", entityColumn = "userId")
    val dailyProgress: List<DailyProgressEntity>,

    @Relation(parentColumn = "id", entityColumn = "userId")
    val achievements: List<AchievementEntity>,

    @Relation(parentColumn = "id", entityColumn = "userId")
    val adjustmentLogs: List<AdjustmentLogEntity>
)

data class DailyProgressWithSessions(
    @Embedded val dailyProgress: DailyProgressEntity,

    @Relation(parentColumn = "id", entityColumn = "dailyProgressId")
    val sessions: List<SessionEntity>
)

data class SessionWithRecords(
    @Embedded val session: SessionEntity,

    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val records: List<ExerciseRecordEntity>
)
