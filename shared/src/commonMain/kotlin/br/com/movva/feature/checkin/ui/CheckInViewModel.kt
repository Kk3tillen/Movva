package br.com.movva.feature.checkin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.auth.domain.AuthRepository
import br.com.movva.feature.checkin.domain.CheckInRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CheckInUiState {
    data object Carregando : CheckInUiState
    data class Vazio(val nomeUsuario: String = "") : CheckInUiState
    data class Sucesso(val nomeUsuario: String, val streakDias: Int) : CheckInUiState
    data class Erro(val mensagem: String) : CheckInUiState
}

class CheckInViewModel(
    private val checkInRepository: CheckInRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Carregando)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.value = CheckInUiState.Carregando
            try {
                val nome = authRepository.currentUser()?.nome ?: ""
                val streak = checkInRepository.getStreakAtual()
                _uiState.value = if (streak == 0) {
                    CheckInUiState.Vazio(nomeUsuario = nome)
                } else {
                    CheckInUiState.Sucesso(nomeUsuario = nome, streakDias = streak)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = CheckInUiState.Erro(
                    e.message ?: "Não foi possível carregar sua sequência"
                )
            }
        }
    }
}
