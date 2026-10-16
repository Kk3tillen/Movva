package br.com.movva.components.treino.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.treino.data.TreinoRepository
import br.com.movva.core.util.dataDeHojeIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TreinoDetalheUiState {
    data object Loading : TreinoDetalheUiState
    data class Erro(val mensagem: String) : TreinoDetalheUiState
    data class Dados(
        val registro: RegistroTreino,
        // houve mudança desde que a tela abriu (ou desde a execução de hoje já salva)
        val modificado: Boolean = false,
        val finalizando: Boolean = false,
        val erroFinalizar: String? = null,
    ) : TreinoDetalheUiState {
        // só faz sentido salvar quando algo mudou e há ao menos uma série feita
        val podeFinalizar: Boolean get() = modificado && registro.temSeriesRegistradas
    }
}

class TreinoDetalheViewModel(
    private val treinoId: String,
    private val treinoRepository: TreinoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TreinoDetalheUiState>(TreinoDetalheUiState.Loading)
    val uiState: StateFlow<TreinoDetalheUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _uiState.value = TreinoDetalheUiState.Loading
        viewModelScope.launch {
            treinoRepository.getTreinoDetalhe(treinoId)
                .onSuccess { _uiState.value = TreinoDetalheUiState.Dados(RegistroTreino.iniciar(it)) }
                .onFailure { e ->
                    _uiState.value = TreinoDetalheUiState.Erro(e.message ?: "Não foi possível carregar o treino")
                }
        }
    }

    fun onExercicioClick(exercicioId: String) = atualizarRegistro { it.alternarExpandido(exercicioId) }
    fun onRepsChange(exercicioId: String, indice: Int, valor: String) =
        atualizarRegistro { it.alterarReps(exercicioId, indice, valor) }
    fun onCargaChange(exercicioId: String, indice: Int, valor: String) =
        atualizarRegistro { it.alterarCarga(exercicioId, indice, valor) }
    fun onSerieCheck(exercicioId: String, indice: Int) = atualizarRegistro { it.alternarSerie(exercicioId, indice) }
    fun onAdicionarSerie(exercicioId: String) = atualizarRegistro { it.adicionarSerie(exercicioId) }
    fun onRemoverSerie(exercicioId: String, indice: Int) = atualizarRegistro { it.removerSerie(exercicioId, indice) }
    fun onReiniciar() = atualizarRegistro { it.reiniciar() }

    private fun atualizarRegistro(transformar: (RegistroTreino) -> RegistroTreino) {
        _uiState.update { atual ->
            if (atual is TreinoDetalheUiState.Dados && !atual.finalizando) {
                val novo = transformar(atual.registro)
                // expandir/recolher card não conta como alteração
                val alterou = novo.exercicios != atual.registro.exercicios
                atual.copy(registro = novo, modificado = atual.modificado || alterou, erroFinalizar = null)
            } else {
                atual
            }
        }
    }

    fun finalizar() {
        val atual = _uiState.value as? TreinoDetalheUiState.Dados ?: return
        if (atual.finalizando || !atual.podeFinalizar) return

        _uiState.value = atual.copy(finalizando = true, erroFinalizar = null)
        viewModelScope.launch {
            treinoRepository.finalizarTreino(treinoId, atual.registro.exerciciosRegistrados())
                .onSuccess {
                    // continua na tela, agora mostrando o treino como feito hoje
                    _uiState.update {
                        val dados = it as TreinoDetalheUiState.Dados
                        val treino = dados.registro.treino.copy(feitoHoje = true, ultimaExecucao = dataDeHojeIso())
                        dados.copy(
                            registro = dados.registro.copy(treino = treino, expandidoId = null),
                            modificado = false,
                            finalizando = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        (it as TreinoDetalheUiState.Dados).copy(
                            finalizando = false,
                            erroFinalizar = e.message ?: "Não foi possível finalizar o treino",
                        )
                    }
                }
        }
    }
}
