package br.com.movva.components.treino.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.treino.data.TreinoRepository
import br.com.movva.components.treino.data.TreinosResultado
import br.com.movva.components.treino.domain.NovoTreinoErros
import br.com.movva.components.treino.domain.NovoTreinoForm
import br.com.movva.components.treino.domain.Treino
import br.com.movva.components.treino.domain.validarNovoTreino
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TreinosUiState {
    data object Loading : TreinosUiState
    data class Dados(val treinos: List<Treino>) : TreinosUiState
    data class Erro(val mensagem: String) : TreinosUiState
}

data class NovoTreinoUiState(
    val form: NovoTreinoForm = NovoTreinoForm(),
    val erros: NovoTreinoErros = NovoTreinoErros(),
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val erroGeral: String? = null,
)

class TreinoViewModel(
    private val treinoRepository: TreinoRepository
) : ViewModel() {

    private val _treinosState = MutableStateFlow<TreinosUiState>(TreinosUiState.Loading)
    val treinosState: StateFlow<TreinosUiState> = _treinosState.asStateFlow()

    private val _formState = MutableStateFlow(NovoTreinoUiState())
    val formState: StateFlow<NovoTreinoUiState> = _formState.asStateFlow()

    private var listaJob: Job? = null
    private var tentouSalvar = false

    init {
        carregarTreinos()
    }

    fun carregarTreinos() {
        listaJob?.cancel()
        listaJob = viewModelScope.launch {
            treinoRepository.getTreinos().collect { resultado ->
                _treinosState.value = when (resultado) {
                    is TreinosResultado.Sucesso -> TreinosUiState.Dados(resultado.treinos)
                    is TreinosResultado.Erro -> TreinosUiState.Erro(resultado.mensagem)
                }
            }
        }
    }

    fun onNomeChange(valor: String) = atualizarForm { it.copy(nome = valor) }
    fun onTipoChange(valor: String) = atualizarForm { it.copy(tipo = valor) }
    fun onDuracaoChange(valor: String) = atualizarForm { it.copy(duracao = valor) }
    fun onDataChange(valor: String) = atualizarForm { it.copy(data = valor) }
    fun onObservacoesChange(valor: String) = atualizarForm { it.copy(observacoes = valor) }

    private fun atualizarForm(transformar: (NovoTreinoForm) -> NovoTreinoForm) {
        _formState.update { atual ->
            val novoForm = transformar(atual.form)
            atual.copy(
                form = novoForm,
                // só mostra erros depois da primeira tentativa de salvar
                erros = if (tentouSalvar) validarNovoTreino(novoForm) else atual.erros,
                salvo = false,
                erroGeral = null,
            )
        }
    }

    fun salvar() {
        if (_formState.value.salvando) return

        tentouSalvar = true
        val form = _formState.value.form
        val erros = validarNovoTreino(form)
        _formState.update { it.copy(erros = erros) }
        if (erros.temErro) return

        _formState.update { it.copy(salvando = true, erroGeral = null) }
        viewModelScope.launch {
            treinoRepository.salvar(
                nome = form.nome.trim(),
                tipo = form.tipo.trim(),
                duracaoMin = form.duracao.trim().toIntOrNull(),
                data = form.data.trim(),
                observacoes = form.observacoes.trim().ifBlank { null },
            ).onSuccess {
                tentouSalvar = false
                _formState.value = NovoTreinoUiState(salvo = true)
                carregarTreinos()
            }.onFailure { e ->
                _formState.update {
                    it.copy(
                        salvando = false,
                        erroGeral = e.message ?: "Não foi possível salvar o treino"
                    )
                }
            }
        }
    }

    fun consumirSalvo() {
        _formState.update { it.copy(salvo = false) }
    }
}