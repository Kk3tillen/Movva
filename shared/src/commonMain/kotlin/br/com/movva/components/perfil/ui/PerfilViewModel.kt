package br.com.movva.components.perfil.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.perfil.domain.Avatar
import br.com.movva.components.perfil.domain.PerfilRepository
import br.com.movva.components.perfil.domain.PerfilResultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PerfilUiState {
    data object Loading : PerfilUiState
    data object Vazio : PerfilUiState
    data class Dados(val nome: String, val userTag: String, val avatarUrl: String) : PerfilUiState
    data class Erro(val mensagem: String) : PerfilUiState
}

class PerfilViewModel(
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null

    init {
        carregarPerfil()
    }

    fun carregarPerfil() {
        carregarJob?.cancel()
        _uiState.value = PerfilUiState.Loading
        carregarJob = viewModelScope.launch {
            perfilRepository.getPerfil().collect { resultado ->
                _uiState.value = when (resultado) {
                    is PerfilResultado.Sucesso -> {
                        val perfil = resultado.perfil
                        if (perfil.nome.isBlank() && perfil.userTag.isBlank()) {
                            PerfilUiState.Vazio
                        } else {
                            PerfilUiState.Dados(
                                nome = perfil.nome,
                                userTag = perfil.userTag,
                                avatarUrl = Avatar.url(
                                    perfil.avatarId.ifBlank { perfil.userTag }
                                )
                            )
                        }
                    }
                    is PerfilResultado.Erro -> PerfilUiState.Erro(resultado.mensagem)
                }
            }
        }
    }
}
