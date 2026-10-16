package br.com.movva.components.dieta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.core.util.dataDeHojeIso
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DietasUiState {
    data object Loading : DietasUiState
    data class Dados(val dietas: List<Dieta>) : DietasUiState
    data class Erro(val mensagem: String) : DietasUiState
}

class DietaViewModel(
    private val dietaRepository: DietaRepository
) : ViewModel() {

    private val _dietasState = MutableStateFlow<DietasUiState>(DietasUiState.Loading)
    val dietasState: StateFlow<DietasUiState> = _dietasState.asStateFlow()

    private var listaJob: Job? = null

    fun carregarDietas() {
        listaJob?.cancel()
        listaJob = viewModelScope.launch {
            dietaRepository.getDietas().collect { resultado ->
                _dietasState.value = when (resultado) {
                    is DietasResultado.Sucesso -> DietasUiState.Dados(resultado.dietas)
                    is DietasResultado.Erro -> DietasUiState.Erro(resultado.mensagem)
                }
            }
        }
    }

    fun onMarcarFeita(dieta: Dieta) {
        val feita = !dieta.feitaHoje
        substituir(dieta.copy(feitaHoje = feita, ultimaVezFeita = if (feita) dataDeHojeIso() else dieta.ultimaVezFeita))
        viewModelScope.launch {
            dietaRepository.marcarFeitaHoje(dieta.id, feita)
                .onSuccess { if (!feita) carregarDietas() }
                .onFailure { substituir(dieta) }
        }
    }

    private fun substituir(dieta: Dieta) = _dietasState.update { estado ->
        if (estado is DietasUiState.Dados) {
            DietasUiState.Dados(estado.dietas.map { if (it.id == dieta.id) dieta else it })
        } else {
            estado
        }
    }
}
