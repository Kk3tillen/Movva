package br.com.movva.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface StartDestination {
    data object Loading : StartDestination
    data object Home : StartDestination
    data object Login : StartDestination
}

class AuthGateViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val destination: StateFlow<StartDestination> = _destination

    init {
        viewModelScope.launch {
            val status = authRepository.sessionStatus
                .filter { it !is SessionStatus.Initializing }
                .first()

            _destination.value = when (status) {
                is SessionStatus.Authenticated -> StartDestination.Home
                else -> StartDestination.Login
            }
        }
    }
}