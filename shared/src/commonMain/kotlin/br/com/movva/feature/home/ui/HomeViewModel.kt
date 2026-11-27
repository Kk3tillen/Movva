package br.com.movva.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.auth.domain.AuthRepository
import br.com.movva.components.dieta.domain.DietaRepository
import br.com.movva.components.dieta.domain.DietasResultado
import br.com.movva.components.dieta.domain.resumoDeHoje
import br.com.movva.feature.checkin.domain.CheckInRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Carregando : HomeUiState
    data class Vazio(val nomeUsuario: String = "") : HomeUiState
    data class Sucesso(
        val nomeUsuario: String,
        val streakDias: Int,
        val treinoFeitoHoje: Boolean,
        val dietaSeguidaHoje: Boolean,
        val kcalHoje: Double,
    ) : HomeUiState
    data class Erro(val mensagem: String) : HomeUiState
}

class HomeViewModel(
    private val checkInRepository: CheckInRepository,
    private val dietaRepository: DietaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Carregando)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Carregando
            try {
                val nome = authRepository.currentUser()?.nome ?: ""
                val checkIn = checkInRepository.getCheckInDeHoje()
                val streak = checkInRepository.getStreakAtual()
                val kcalHoje = when (val resultado = dietaRepository.getDietas().first()) {
                    is DietasResultado.Sucesso -> resultado.dietas.resumoDeHoje().kcal
                    is DietasResultado.Erro -> {
                        if (checkIn == null && streak == 0) {
                            _uiState.value = HomeUiState.Erro(resultado.mensagem)
                            return@launch
                        }
                        0.0
                    }
                }

                val semAtividade = streak == 0 && checkIn == null && kcalHoje == 0.0
                _uiState.value = if (semAtividade) {
                    HomeUiState.Vazio(nomeUsuario = nome)
                } else {
                    HomeUiState.Sucesso(
                        nomeUsuario = nome,
                        streakDias = streak,
                        treinoFeitoHoje = checkIn?.treinoFeito == true,
                        dietaSeguidaHoje = checkIn?.dietaSeguida == true,
                        kcalHoje = kcalHoje,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Erro(
                    e.message ?: "Não foi possível carregar o resumo de hoje"
                )
            }
        }
    }
}
