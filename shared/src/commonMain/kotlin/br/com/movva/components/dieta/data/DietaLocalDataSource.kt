package br.com.movva.components.dieta.data

import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.shared.db.DietaEntity
import br.com.movva.shared.db.MovvaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface DietaLocalDataSource {
    suspend fun getDietas(userId: String): List<Dieta>
    suspend fun getDieta(userId: String, id: String): Dieta?
    suspend fun getDietasDoDia(userId: String, data: String): List<Dieta>
    suspend fun getNaoSincronizados(userId: String): List<Dieta>
    suspend fun salvar(userId: String, dieta: Dieta, sincronizado: Boolean)
    suspend fun marcarSincronizado(id: String)
    suspend fun getUltimasFeitas(userId: String): Map<String, String>
    suspend fun salvarFeita(userId: String, marcacao: RefeicaoFeita, sincronizado: Boolean)
    suspend fun getFeitasNaoSincronizadas(userId: String): List<RefeicaoFeita>
    suspend fun marcarFeitaSincronizada(dietaId: String, data: String)
}

data class RefeicaoFeita(
    val dietaId: String,
    val data: String,
    val feita: Boolean,
)

class DietaLocalDataSourceImpl(
    private val database: MovvaDatabase
) : DietaLocalDataSource {

    override suspend fun getDietas(userId: String): List<Dieta> =
        withContext(Dispatchers.Default) {
            database.dietaQueries.selectDietas(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getDieta(userId: String, id: String): Dieta? =
        withContext(Dispatchers.Default) {
            database.dietaQueries.selectDietaPorId(userId, id).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getDietasDoDia(userId: String, data: String): List<Dieta> =
        withContext(Dispatchers.Default) {
            database.dietaQueries.selectDietasDoDia(userId, data).executeAsList().map { it.toDomain() }
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
                sincronizado = if (sincronizado) 1L else 0L,
                itensJson = Json.encodeToString(dieta.itens)
            )
        }
    }

    override suspend fun marcarSincronizado(id: String) {
        withContext(Dispatchers.Default) {
            database.dietaQueries.marcarSincronizado(id)
        }
    }

    override suspend fun getUltimasFeitas(userId: String): Map<String, String> =
        withContext(Dispatchers.Default) {
            database.refeicaoFeitaQueries.selectUltimasFeitas(userId).executeAsList()
                .mapNotNull { linha -> linha.ultima?.let { linha.dietaId to it } }
                .toMap()
        }

    override suspend fun salvarFeita(userId: String, marcacao: RefeicaoFeita, sincronizado: Boolean) {
        withContext(Dispatchers.Default) {
            database.refeicaoFeitaQueries.upsertFeita(
                dietaId = marcacao.dietaId,
                userId = userId,
                dataFeita = marcacao.data,
                feita = if (marcacao.feita) 1L else 0L,
                sincronizado = if (sincronizado) 1L else 0L
            )
        }
    }

    override suspend fun getFeitasNaoSincronizadas(userId: String): List<RefeicaoFeita> =
        withContext(Dispatchers.Default) {
            database.refeicaoFeitaQueries.selectNaoSincronizadas(userId).executeAsList().map {
                RefeicaoFeita(dietaId = it.dietaId, data = it.dataFeita, feita = it.feita == 1L)
            }
        }

    override suspend fun marcarFeitaSincronizada(dietaId: String, data: String) {
        withContext(Dispatchers.Default) {
            database.refeicaoFeitaQueries.marcarSincronizada(dietaId, data)
        }
    }
}

private fun DietaEntity.toDomain() = Dieta(
    id = id,
    tipoRefeicao = tipoRefeicao,
    descricao = descricao,
    data = dataRefeicao,
    itens = Json.decodeFromString(itensJson)
)