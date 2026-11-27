package br.com.movva.components.treino.domain

import kotlinx.coroutines.flow.Flow

sealed interface TreinosResultado {
    data class Sucesso(val treinos: List<Treino>) : TreinosResultado
    data class Erro(val mensagem: String) : TreinosResultado
}

interface TreinoRepository {
    fun getTreinos(): Flow<TreinosResultado>
    suspend fun salvar(
        nome: String,
        tipo: String,
        duracaoMin: Int?,
        observacoes: String?,
        exercicios: List<Exercicio> = emptyList()
    ): Result<Unit>
    suspend fun getTreinoDetalhe(id: String): Result<TreinoDetalhe>
    suspend fun finalizarTreino(treinoId: String, exercicios: List<Exercicio>): Result<Unit>
}
