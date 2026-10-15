package br.com.movva.components.dieta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.NovaDietaErros
import br.com.movva.components.dieta.domain.NovaDietaForm
import br.com.movva.components.dieta.domain.validarNovaDieta
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

data class NovaDietaUiState(
    val form: NovaDietaForm = NovaDietaForm(),
    val erros: NovaDietaErros = NovaDietaErros(),
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val erroGeral: String? = null,
)

class DietaViewModel(
    private val dietaRepository: DietaRepository
) : ViewModel() {

    private val _dietasState = MutableStateFlow<DietasUiState>(DietasUiState.Loading)
    val dietasState: StateFlow<DietasUiState> = _dietasState.asStateFlow()

    private val _formState = MutableStateFlow(NovaDietaUiState())
    val formState: StateFlow<NovaDietaUiState> = _formState.asStateFlow()

    private var listaJob: Job? = null
    private var tentouSalvar = false

    init {
        carregarDietas()
    }

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

    fun onTipoRefeicaoChange(valor: String) = atualizarForm { it.copy(tipoRefeicao = valor) }
    fun onDescricaoChange(valor: String) = atualizarForm { it.copy(descricao = valor) }
    fun onDataChange(valor: String) = atualizarForm { it.copy(data = valor) }

    private fun atualizarForm(transformar: (NovaDietaForm) -> NovaDietaForm) {
        _formState.update { atual ->
            val novoForm = transformar(atual.form)
            atual.copy(
                form = novoForm,
                // só mostra erros depois da primeira tentativa de salvar
                erros = if (tentouSalvar) validarNovaDieta(novoForm) else atual.erros,
                salvo = false,
                erroGeral = null,
            )
        }
    }

    fun salvar() {
        if (_formState.value.salvando) return

        tentouSalvar = true
        val form = _formState.value.form
        val erros = validarNovaDieta(form)
        _formState.update { it.copy(erros = erros) }
        if (erros.temErro) return

        _formState.update { it.copy(salvando = true, erroGeral = null) }
        viewModelScope.launch {
            dietaRepository.salvar(
                tipoRefeicao = form.tipoRefeicao.trim(),
                descricao = form.descricao.trim(),
                data = form.data.trim(),
            ).onSuccess {
                tentouSalvar = false
                _formState.value = NovaDietaUiState(salvo = true)
                carregarDietas()
            }.onFailure { e ->
                _formState.update {
                    it.copy(
                        salvando = false,
                        erroGeral = e.message ?: "Não foi possível salvar a refeição"
                    )
                }
            }
        }
    }

    fun consumirSalvo() {
        _formState.update { it.copy(salvo = false) }
    }
}