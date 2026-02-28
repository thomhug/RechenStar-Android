package ch.rechenstar.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.rechenstar.app.data.local.entity.UserEntity
import ch.rechenstar.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val users: StateFlow<List<UserEntity>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProfile(name: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.createUser(name)
            onCreated(user.id)
        }
    }
}
