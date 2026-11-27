package br.com.movva.components.perfil.domain

import kotlinx.coroutines.flow.Flow

sealed interface PerfilResultado {
    data class Sucesso(val perfil: Perfil) : PerfilResultado
    data class Erro(val mensagem: String) : PerfilResultado
}

interface PerfilRepository {
    fun getPerfil(): Flow<PerfilResultado>
}
