package br.com.movva.components.dieta.data

import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.shared.db.DietaEntity
import br.com.movva.shared.db.MovvaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DietaLocalDataSource {
    suspend fun getDietas(userId: String): List<Dieta>
    suspend fun getNaoSincronizados(userId: String): List<Dieta>
    suspend fun salvar(userId: String, dieta: Dieta, sincronizado: Boolean)
    suspend fun marcarSincronizado(id: String)
}

class DietaLocalDataSourceImpl(
    private val database: MovvaDatabase
) : DietaLocalDataSource {

    override suspend fun getDietas(userId: String): List<Dieta> =
        withContext(Dispatchers.Default) {
            database.dietaQueries.selectDietas(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getNaoSincronizados(userId: String): List<Dieta> =
        withContext(Dispatchers.Default) {
            database.dietaQueries.selectNaoSincronizados(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun salvar(userId: String, dieta: Dieta, sincronizado: Boolean) {
        withContext(Dispatchers.Default) {
            database.dietaQueries.upsertDieta(
                id = dieta.id,
                userId = userId,
                tipoRefeicao = dieta.tipoRefeicao,
                descricao = dieta.descricao,
                dataRefeicao = dieta.data,
                sincronizado = if (sincronizado) 1L else 0L
            )
        }
    }

    override suspend fun marcarSincronizado(id: String) {
        withContext(Dispatchers.Default) {
            database.dietaQueries.marcarSincronizado(id)
        }
    }
}

private fun DietaEntity.toDomain() = Dieta(
    id = id,
    tipoRefeicao = tipoRefeicao,
    descricao = descricao,
    data = dataRefeicao
)