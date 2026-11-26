package br.com.movva.components.dieta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.data.CheckInRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DietasUiState {
    data object Loading : DietasUiState
    data class Dados(
        val dietas: List<Dieta>,
        val selecionadas: Set<String> = emptySet(),
        val finalizando: Boolean = false,
        val dietaSeguidaHoje: Boolean = false,
    ) : DietasUiState
    data class Erro(val mensagem: String) : DietasUiState
}

class DietaViewModel(
    private val dietaRepository: DietaRepository,
    private val checkInRepository: CheckInRepository,
) : ViewModel() {

    private val _dietasState = MutableStateFlow<DietasUiState>(DietasUiState.Loading)
    val dietasState: StateFlow<DietasUiState> = _dietasState.asStateFlow()

    private val _mensagens = MutableSharedFlow<String>()
    val mensagens: SharedFlow<String> = _mensagens.asSharedFlow()

    private var listaJob: Job? = null

    fun carregarDietas() {
        listaJob?.cancel()
        listaJob = viewModelScope.launch {
            val checkInHoje = checkInRepository.getCheckInDeHoje()
            val dietaSeguidaHoje = checkInHoje?.dietaSeguida == true

            dietaRepository.getDietas().collect { resultado ->
                _dietasState.value = when (resultado) {
                    is DietasResultado.Sucesso -> {
                        val selecionadasAtuais = (_dietasState.value as? DietasUiState.Dados)?.selecionadas ?: emptySet()
                        DietasUiState.Dados(
                            dietas = resultado.dietas,
                            selecionadas = selecionadasAtuais,
                            dietaSeguidaHoje = dietaSeguidaHoje,
                        )
                    }
                    is DietasResultado.Erro -> DietasUiState.Erro(resultado.mensagem)
                }
            }
        }
    }

    fun onAlternarSelecao(dietaId: String) {
        _dietasState.update { estado ->
            if (estado is DietasUiState.Dados) {
                val novas = if (dietaId in estado.selecionadas) {
                    estado.selecionadas - dietaId
                } else {
                    estado.selecionadas + dietaId
                }
                estado.copy(selecionadas = novas)
            } else {
                estado
            }
        }
    }

    fun onFinalizarDieta() {
        val estado = _dietasState.value as? DietasUiState.Dados ?: return
        if (estado.selecionadas.isEmpty() || estado.finalizando) return

        _dietasState.update { if (it is DietasUiState.Dados) it.copy(finalizando = true) else it }

        viewModelScope.launch {
            val hoje = dataDeHojeIso()
            for (dietaId in estado.selecionadas) {
                dietaRepository.marcarFeitaHoje(dietaId, true)
            }
            checkInRepository.marcarDietaSeguida(hoje)
                .onSuccess {
                    _dietasState.update {
                        if (it is DietasUiState.Dados) {
                            it.copy(
                                finalizando = false,
                                dietaSeguidaHoje = true,
                                selecionadas = emptySet(),
                            )
                        } else it
                    }
                    carregarDietas()
                    _mensagens.emit("🔥 Check-in de dieta concluído! Streak mantido.")
                }
                .onFailure { e ->
                    _dietasState.update { if (it is DietasUiState.Dados) it.copy(finalizando = false) else it }
                    _mensagens.emit(e.message ?: "Não foi possível registrar o check-in")
                }
        }
    }
}
