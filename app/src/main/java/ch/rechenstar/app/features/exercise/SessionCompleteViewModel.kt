package ch.rechenstar.app.features.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.repository.ProgressRepository
import ch.rechenstar.app.data.repository.SessionRepository
import ch.rechenstar.app.data.repository.UserRepository
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.domain.service.EngagementService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionCompleteViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _engagement = MutableStateFlow(EngagementResult())
    val engagement: StateFlow<EngagementResult> = _engagement.asStateFlow()

    private var processed = false

    fun processSession(results: List<ExerciseResult>, userId: String, sessionStartTime: Long) {
        if (processed) return
        processed = true

        viewModelScope.launch {
            val result = EngagementService.processSession(
                results = results,
                userId = userId,
                sessionStartTime = sessionStartTime,
                userRepo = userRepository,
                progressRepo = progressRepository,
                sessionRepo = sessionRepository
            )
            _engagement.value = result
        }
    }
}
