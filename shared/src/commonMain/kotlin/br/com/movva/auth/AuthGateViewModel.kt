package br.com.movva.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            _destination.value = if (authRepository.currentUser() != null) {
                StartDestination.Home
            } else {
                StartDestination.Login
            }
        }
    }

    fun onLoginSuccess() {
        _destination.value = StartDestination.Home
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _destination.value = StartDestination.Login
        }
    }
}