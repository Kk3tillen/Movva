package br.com.movva.feature.checkin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.auth.AuthRepository
import br.com.movva.feature.checkin.data.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckInUiState(
    val carregando: Boolean = true,
    val nomeUsuario: String = "",
    val streakDias: Int = 0,
)

class CheckInViewModel(
    private val checkInRepository: CheckInRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val nome = authRepository.currentUser()?.nome ?: ""
            val streak = checkInRepository.getStreakAtual()
            _uiState.update {
                it.copy(
                    carregando = false,
                    nomeUsuario = nome,
                    streakDias = streak,
                )
            }
        }
    }
}
