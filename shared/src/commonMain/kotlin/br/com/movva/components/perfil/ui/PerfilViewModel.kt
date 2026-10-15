package br.com.movva.components.perfil.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.perfil.data.PerfilRepository
import br.com.movva.components.perfil.data.PerfilResultado
import br.com.movva.components.perfil.domain.Avatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PerfilUiState {
    data object Loading : PerfilUiState
    data class Dados(val nome: String, val userTag: String, val avatarUrl: String) : PerfilUiState
    data class Erro(val mensagem: String) : PerfilUiState
}

class PerfilViewModel(
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        carregarPerfil()
    }

    fun carregarPerfil() {
        viewModelScope.launch {
            perfilRepository.getPerfil().collect { resultado ->
                _uiState.value = when (resultado) {
                    is PerfilResultado.Sucesso -> PerfilUiState.Dados(
                        nome = resultado.perfil.nome,
                        userTag = resultado.perfil.userTag,
                        avatarUrl = Avatar.url(
                            resultado.perfil.avatarId.ifBlank { resultado.perfil.userTag }
                        )
                    )
                    is PerfilResultado.Erro -> PerfilUiState.Erro(resultado.mensagem)
                }
            }
        }
    }
}
