package br.com.movva.feature.treino.data

import br.com.movva.feature.treino.domain.Treino
import br.com.movva.shared.db.MovvaDatabase
import br.com.movva.shared.db.TreinoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TreinoLocalDataSource {
    suspend fun getTreinos(userId: String): List<Treino>
    suspend fun getNaoSincronizados(userId: String): List<Treino>
    suspend fun salvar(userId: String, treino: Treino, sincronizado: Boolean)
    suspend fun marcarSincronizado(id: String)
}

class TreinoLocalDataSourceImpl(
    private val database: MovvaDatabase
) : TreinoLocalDataSource {

    override suspend fun getTreinos(userId: String): List<Treino> =
        withContext(Dispatchers.Default) {
            database.treinoQueries.selectTreinos(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getNaoSincronizados(userId: String): List<Treino> =
        withContext(Dispatchers.Default) {
            database.treinoQueries.selectNaoSincronizados(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun salvar(userId: String, treino: Treino, sincronizado: Boolean) {
        withContext(Dispatchers.Default) {
            database.treinoQueries.upsertTreino(
                id = treino.id,
                userId = userId,
                nome = treino.nome,
                tipo = treino.tipo,
                duracaoMin = treino.duracaoMin?.toLong(),
                dataTreino = treino.data,
                observacoes = treino.observacoes,
                sincronizado = if (sincronizado) 1L else 0L
            )
        }
    }

    override suspend fun marcarSincronizado(id: String) {
        withContext(Dispatchers.Default) {
            database.treinoQueries.marcarSincronizado(id)
        }
    }
}

private fun TreinoEntity.toDomain() = Treino(
    id = id,
    nome = nome,
    tipo = tipo,
    duracaoMin = duracaoMin?.toInt(),
    data = dataTreino,
    observacoes = observacoes
)