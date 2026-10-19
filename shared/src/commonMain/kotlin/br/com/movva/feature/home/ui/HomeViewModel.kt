package br.com.movva.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.auth.AuthRepository
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.resumoDeHoje
import br.com.movva.feature.checkin.data.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val carregando: Boolean = true,
    val nomeUsuario: String = "",
    val streakDias: Int = 0,
    val treinoFeitoHoje: Boolean = false,
    val dietaSeguidaHoje: Boolean = false,
    val kcalHoje: Double = 0.0,
)

class HomeViewModel(
    private val checkInRepository: CheckInRepository,
    private val dietaRepository: DietaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }

            val nome = authRepository.currentUser()?.nome ?: ""
            val checkIn = checkInRepository.getCheckInDeHoje()
            val streak = checkInRepository.getStreakAtual()
            val kcalHoje = when (val resultado = dietaRepository.getDietas().first()) {
                is DietasResultado.Sucesso -> resultado.dietas.resumoDeHoje().kcal
                is DietasResultado.Erro -> 0.0
            }

            _uiState.update {
                it.copy(
                    carregando = false,
                    nomeUsuario = nome,
                    streakDias = streak,
                    treinoFeitoHoje = checkIn?.treinoFeito == true,
                    dietaSeguidaHoje = checkIn?.dietaSeguida == true,
                    kcalHoje = kcalHoje,
                )
            }
        }
    }
}
