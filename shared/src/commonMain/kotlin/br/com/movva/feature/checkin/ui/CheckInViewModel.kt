package br.com.movva.feature.checkin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.feature.checkin.data.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckInUiState(
    val carregando: Boolean = true,
    val treinoFeito: Boolean = false,
    val dietaSeguida: Boolean = false,
    val registrado: Boolean = false,
    val alterado: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
)

class CheckInViewModel(
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            val checkIn = checkInRepository.getCheckInDeHoje()
            _uiState.update {
                it.copy(
                    carregando = false,
                    treinoFeito = checkIn?.treinoFeito ?: false,
                    dietaSeguida = checkIn?.dietaSeguida ?: false,
                    registrado = checkIn != null,
                    alterado = false,
                )
            }
        }
    }

    fun onTreinoFeitoChange(valor: Boolean) = atualizarCampo { it.copy(treinoFeito = valor) }
    fun onDietaSeguidaChange(valor: Boolean) = atualizarCampo { it.copy(dietaSeguida = valor) }

    private fun atualizarCampo(transformar: (CheckInUiState) -> CheckInUiState) {
        _uiState.update { atual ->
            val novo = transformar(atual)
            // só marca como "alterado" se já existia um check-in salvo hoje;
            // antes do primeiro salvamento o botão continua como "Fazer Check-in"
            novo.copy(alterado = atual.registrado, erro = null)
        }
    }

    fun registrarCheckIn() {
        if (_uiState.value.salvando) return

        val estado = _uiState.value
        _uiState.update { it.copy(salvando = true, erro = null) }

        viewModelScope.launch {
            checkInRepository.registrarCheckIn(
                treinoFeito = estado.treinoFeito,
                dietaSeguida = estado.dietaSeguida,
            ).onSuccess {
                _uiState.update {
                    it.copy(salvando = false, registrado = true, alterado = false)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(salvando = false, erro = e.message ?: "Não foi possível salvar o check-in")
                }
            }
        }
    }
}
