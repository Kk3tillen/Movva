package br.com.movva.components.dieta.domain

import kotlinx.coroutines.flow.Flow

sealed interface DietasResultado {
    data class Sucesso(val dietas: List<Dieta>) : DietasResultado
    data class Erro(val mensagem: String) : DietasResultado
}

interface DietaRepository {
    fun getDietas(): Flow<DietasResultado>
    suspend fun getDieta(id: String): Result<Dieta>
    suspend fun getRefeicaoDeHoje(tipoRefeicao: String): Dieta?
    suspend fun salvarRefeicao(
        id: String?,
        tipoRefeicao: String,
        itens: List<ItemRefeicao>
    ): Result<Dieta>

    suspend fun marcarFeitaHoje(dietaId: String, feita: Boolean): Result<Unit>
    suspend fun getAlimentosFrequentes(limite: Int): List<String>
}
