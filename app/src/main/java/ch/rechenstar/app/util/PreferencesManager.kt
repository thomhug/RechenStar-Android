package ch.rechenstar.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rechenstar_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        // Active user
        val KEY_ACTIVE_USER_ID = stringPreferencesKey("active_user_id")

        // Gameplay
        val KEY_DIFFICULTY_LEVEL = intPreferencesKey("difficulty_level")
        val KEY_ADAPTIVE_DIFFICULTY = booleanPreferencesKey("adaptive_difficulty")
        val KEY_SESSION_LENGTH = intPreferencesKey("session_length")
        val KEY_DAILY_GOAL = intPreferencesKey("daily_goal")
        val KEY_GAP_FILL_ENABLED = booleanPreferencesKey("gap_fill_enabled")
        val KEY_HIDE_SKIP_BUTTON = booleanPreferencesKey("hide_skip_button")
        val KEY_AUTO_SHOW_ANSWER_SECONDS = intPreferencesKey("auto_show_answer_seconds")

        // Audio & Haptics
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")

        // Visual
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val KEY_LARGER_TEXT = booleanPreferencesKey("larger_text")
        val KEY_COLOR_BLIND_MODE = stringPreferencesKey("color_blind_mode")

        // Categories
        val KEY_ENABLED_CATEGORIES = stringPreferencesKey("enabled_categories")

        // Parental
        val KEY_TIME_LIMIT_MINUTES = intPreferencesKey("time_limit_minutes")
        val KEY_TIME_LIMIT_ENABLED = booleanPreferencesKey("time_limit_enabled")
        val KEY_BREAK_REMINDER = booleanPreferencesKey("break_reminder")
        val KEY_BREAK_INTERVAL_SECONDS = intPreferencesKey("break_interval_seconds")
    }

    val activeUserId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_USER_ID]
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    val sessionLength: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_SESSION_LENGTH] ?: 10
    }

    val dailyGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DAILY_GOAL] ?: 20
    }

    val difficultyLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DIFFICULTY_LEVEL] ?: 2
    }

    val adaptiveDifficulty: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ADAPTIVE_DIFFICULTY] ?: true
    }

    val gapFillEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAP_FILL_ENABLED] ?: true
    }

    val enabledCategories: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ENABLED_CATEGORIES] ?: "addition_10,subtraction_10"
    }

    val reducedMotion: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REDUCED_MOTION] ?: false
    }

    val colorBlindMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_COLOR_BLIND_MODE] ?: "none"
    }

    suspend fun setActiveUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId != null) prefs[KEY_ACTIVE_USER_ID] = userId
            else prefs.remove(KEY_ACTIVE_USER_ID)
        }
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    suspend fun <T> remove(key: Preferences.Key<T>) {
        context.dataStore.edit { prefs -> prefs.remove(key) }
    }
}
