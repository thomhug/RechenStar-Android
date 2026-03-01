package ch.rechenstar.app.domain.service

import ch.rechenstar.app.data.local.entity.ExerciseRecordEntity
import ch.rechenstar.app.data.local.entity.SessionEntity
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.SessionRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.AchievementType
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.model.Level
import ch.rechenstar.app.domain.model.OperationType
import ch.rechenstar.app.features.exercise.EngagementResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.UUID

object EngagementService {

    suspend fun processSession(
        results: List<ExerciseResult>,
        userId: String,
        sessionStartTime: Long,
        userRepo: UserRepository,
        progressRepo: ProgressRepository,
        sessionRepo: SessionRepository
    ): EngagementResult {
        val user = userRepo.getUserById(userId) ?: return EngagementResult()
        val prefs = userRepo.getPreferencesSync(userId)
        val dailyGoal = prefs?.dailyGoal ?: 20

        // Daily progress
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val dailyProgress = progressRepo.getOrCreateForDate(userId, todayStart)

        val exercisesBefore = dailyProgress.exercisesCompleted
        val wasAlreadyReached = exercisesBefore >= dailyGoal

        val attempted = results.filter { !it.wasSkipped }

        // Update daily progress
        val updatedProgress = dailyProgress.copy(
            exercisesCompleted = dailyProgress.exercisesCompleted + attempted.size,
            correctAnswers = dailyProgress.correctAnswers + attempted.count { it.isCorrect },
            totalTime = dailyProgress.totalTime + attempted.sumOf { it.timeSpent },
            sessionsCount = dailyProgress.sessionsCount + 1
        )
        progressRepo.updateProgress(updatedProgress)

        // Persist session
        val sessionId = UUID.randomUUID().toString()
        val endTime = System.currentTimeMillis()
        val totalStars = results.sumOf { it.stars }

        val sessionEntity = SessionEntity(
            id = sessionId,
            dailyProgressId = updatedProgress.id,
            startTime = sessionStartTime,
            endTime = endTime,
            isCompleted = true,
            sessionGoal = results.size,
            correctCount = attempted.count { it.isCorrect },
            totalCount = attempted.size,
            starsEarned = totalStars,
            additionCorrect = attempted.count {
                it.isCorrect && it.exercise.type == OperationType.ADDITION
            },
            additionTotal = attempted.count {
                it.exercise.type == OperationType.ADDITION
            },
            subtractionCorrect = attempted.count {
                it.isCorrect && it.exercise.type == OperationType.SUBTRACTION
            },
            subtractionTotal = attempted.count {
                it.exercise.type == OperationType.SUBTRACTION
            }
        )
        sessionRepo.insertSession(sessionEntity)

        // Persist exercise records
        val records = results.map { result ->
            ExerciseRecordEntity(
                id = result.id.toString(),
                sessionId = sessionId,
                exerciseSignature = result.exercise.signature,
                operationType = result.exercise.type.rawValue,
                category = result.exercise.category.rawValue,
                firstNumber = result.exercise.firstNumber,
                secondNumber = result.exercise.secondNumber,
                isCorrect = result.isCorrect,
                timeSpent = result.timeSpent,
                attempts = result.attempts,
                wasSkipped = result.wasSkipped,
                difficulty = result.exercise.difficulty.level
            )
        }
        sessionRepo.insertRecords(records)

        // Update user stats
        val newTotalExercises = user.totalExercises + attempted.size
        val newTotalStars = user.totalStars + totalStars
        userRepo.updateStats(userId, newTotalExercises, newTotalStars)

        // Update streak
        val (newStreak, newLongest, isNewStreak) = updateStreak(
            user.currentStreak, user.longestStreak, user.lastActiveAt
        )
        userRepo.updateStreak(userId, newStreak, newLongest)

        // Check achievements
        val achievements = userRepo.getAchievementsSync(userId)
        val sessionDuration = (endTime - sessionStartTime) / 1000.0
        val allRecords = sessionRepo.getAllRecords(userId)
        val newlyUnlocked = checkAchievements(
            achievements = achievements,
            totalExercises = newTotalExercises,
            totalStars = newTotalStars,
            currentStreak = newStreak,
            sessionStartTime = sessionStartTime,
            sessionDuration = sessionDuration,
            results = results,
            dailyExercises = updatedProgress.exercisesCompleted,
            allRecords = allRecords,
            userRepo = userRepo
        )

        // Update lastActive
        userRepo.updateLastActive(userId)

        // Level up check
        val levelBefore = Level.current(user.totalExercises)
        val levelAfter = Level.current(newTotalExercises)
        val newLevel = if (levelAfter != levelBefore) levelAfter else null

        // Daily goal crossing
        val exercisesAfter = exercisesBefore + attempted.size
        val goalJustReached = !wasAlreadyReached && exercisesAfter >= dailyGoal

        return EngagementResult(
            newlyUnlockedAchievements = newlyUnlocked,
            currentStreak = newStreak,
            isNewStreak = isNewStreak,
            dailyGoalReached = goalJustReached,
            newLevel = newLevel
        )
    }

    fun updateStreak(
        currentStreak: Int,
        longestStreak: Int,
        lastActiveAt: Long
    ): Triple<Int, Int, Boolean> {
        val lastActiveDate = Instant.ofEpochMilli(lastActiveAt)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()

        if (lastActiveDate == today) {
            return Triple(currentStreak, longestStreak, false)
        }

        val newStreak = if (lastActiveDate == today.minusDays(1)) {
            currentStreak + 1
        } else {
            1
        }

        val newLongest = maxOf(longestStreak, newStreak)
        return Triple(newStreak, newLongest, true)
    }

    private suspend fun checkAchievements(
        achievements: List<ch.rechenstar.app.data.local.entity.AchievementEntity>,
        totalExercises: Int,
        totalStars: Int,
        currentStreak: Int,
        sessionStartTime: Long,
        sessionDuration: Double,
        results: List<ExerciseResult>,
        dailyExercises: Int,
        allRecords: List<ExerciseRecordEntity>,
        userRepo: UserRepository
    ): List<AchievementType> {
        val newlyUnlocked = mutableListOf<AchievementType>()

        for (achievement in achievements) {
            if (achievement.unlockedAt != null) continue

            val type = AchievementType.fromRawValue(achievement.typeRawValue) ?: continue

            val (met, progress) = evaluateAchievement(
                type = type,
                totalExercises = totalExercises,
                totalStars = totalStars,
                currentStreak = currentStreak,
                sessionStartTime = sessionStartTime,
                sessionDuration = sessionDuration,
                results = results,
                currentProgress = achievement.progress,
                dailyExercises = dailyExercises,
                allRecords = allRecords
            )

            val newProgress = maxOf(achievement.progress, progress)

            if (met) {
                userRepo.updateAchievement(
                    achievement.copy(
                        progress = achievement.target,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
                newlyUnlocked.add(type)
            } else if (newProgress != achievement.progress) {
                userRepo.updateAchievement(achievement.copy(progress = newProgress))
            }
        }

        return newlyUnlocked
    }

    fun evaluateAchievement(
        type: AchievementType,
        totalExercises: Int = 0,
        totalStars: Int = 0,
        currentStreak: Int = 0,
        sessionStartTime: Long = 0,
        sessionDuration: Double = 0.0,
        results: List<ExerciseResult> = emptyList(),
        currentProgress: Int = 0,
        dailyExercises: Int = 0,
        allRecords: List<ExerciseRecordEntity> = emptyList()
    ): Pair<Boolean, Int> {
        val attempted = results.filter { !it.wasSkipped }

        return when (type) {
            AchievementType.EXERCISES_10 ->
                Pair(totalExercises >= 10, minOf(totalExercises, 10))

            AchievementType.EXERCISES_50 ->
                Pair(totalExercises >= 50, minOf(totalExercises, 50))

            AchievementType.EXERCISES_100 ->
                Pair(totalExercises >= 100, minOf(totalExercises, 100))

            AchievementType.EXERCISES_500 ->
                Pair(totalExercises >= 500, minOf(totalExercises, 500))

            AchievementType.STREAK_3 ->
                Pair(currentStreak >= 3, minOf(currentStreak, 3))

            AchievementType.STREAK_7 ->
                Pair(currentStreak >= 7, minOf(currentStreak, 7))

            AchievementType.STREAK_30 ->
                Pair(currentStreak >= 30, minOf(currentStreak, 30))

            AchievementType.PERFECT_10 -> {
                val isPerfect = attempted.all { it.isCorrect } && attempted.size >= 10
                val newProgress = if (isPerfect) currentProgress + 1 else currentProgress
                Pair(newProgress >= 10, newProgress)
            }

            AchievementType.ALL_STARS ->
                Pair(totalStars >= 100, minOf(totalStars, 100))

            AchievementType.SPEED_DEMON -> {
                val met = attempted.size >= 10 && sessionDuration < 120
                Pair(met, if (met) 1 else 0)
            }

            AchievementType.EARLY_BIRD -> {
                val cal = Calendar.getInstance().apply { timeInMillis = sessionStartTime }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val met = hour < 8
                Pair(met, if (met) 1 else 0)
            }

            AchievementType.NIGHT_OWL -> {
                val cal = Calendar.getInstance().apply { timeInMillis = sessionStartTime }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val met = hour >= 20
                Pair(met, if (met) 1 else 0)
            }

            AchievementType.CATEGORY_MASTER -> {
                val grouped = allRecords.filter { !it.wasSkipped }.groupBy { it.category }
                val met = grouped.any { (_, catRecords) ->
                    catRecords.size >= 20 &&
                        catRecords.count { it.isCorrect }.toDouble() / catRecords.size >= 0.9
                }
                Pair(met, if (met) 1 else 0)
            }

            AchievementType.VARIETY -> {
                val categories = attempted.map { it.exercise.category }.toSet()
                val met = categories.size >= 4
                Pair(met, if (met) 1 else 0)
            }

            AchievementType.ACCURACY_STREAK -> {
                val sessionAccuracy = if (attempted.isEmpty()) 0.0
                else attempted.count { it.isCorrect }.toDouble() / attempted.size
                val newProgress = if (sessionAccuracy >= 0.8) currentProgress + 1 else 0
                Pair(newProgress >= 3, newProgress)
            }

            AchievementType.DAILY_CHAMPION ->
                Pair(dailyExercises >= 100, minOf(dailyExercises, 100))
        }
    }
}
