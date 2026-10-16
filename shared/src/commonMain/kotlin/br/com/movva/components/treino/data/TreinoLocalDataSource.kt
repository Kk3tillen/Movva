package br.com.movva.components.treino.data

import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.Treino
import br.com.movva.shared.db.ExecucaoTreinoEntity
import br.com.movva.shared.db.ExercicioEntity
import br.com.movva.shared.db.MovvaDatabase
import br.com.movva.shared.db.TreinoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface TreinoLocalDataSource {
    suspend fun getTreinos(userId: String): List<Treino>
    suspend fun getTreino(userId: String, id: String): Treino?
    suspend fun getNaoSincronizados(userId: String): List<Treino>
    suspend fun salvar(userId: String, treino: Treino, sincronizado: Boolean)
    suspend fun marcarSincronizado(id: String)
    suspend fun getExercicios(treinoId: String): List<Exercicio>
    suspend fun getExerciciosNaoSincronizados(userId: String): List<ExercicioDoTreino>
    suspend fun salvarExercicios(userId: String, treinoId: String, exercicios: List<Exercicio>, sincronizado: Boolean)
    suspend fun marcarExercicioSincronizado(id: String)
    suspend fun getExecucao(treinoId: String, data: String): ExecucaoTreino?
    suspend fun getUltimaExecucao(treinoId: String): String?
    suspend fun getUltimasExecucoes(userId: String): Map<String, String>
    suspend fun getExecucoesNaoSincronizadas(userId: String): List<ExecucaoTreino>
    suspend fun salvarExecucao(userId: String, execucao: ExecucaoTreino, sincronizado: Boolean)
    suspend fun marcarExecucaoSincronizada(treinoId: String, data: String)
}

// Exercício com o vínculo ao treino e a posição na lista, usado na sincronização.
data class ExercicioDoTreino(
    val treinoId: String,
    val ordem: Int,
    val exercicio: Exercicio,
)

class TreinoLocalDataSourceImpl(
    private val database: MovvaDatabase
) : TreinoLocalDataSource {

    override suspend fun getTreinos(userId: String): List<Treino> =
        withContext(Dispatchers.Default) {
            database.treinoQueries.selectTreinos(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getTreino(userId: String, id: String): Treino? =
        withContext(Dispatchers.Default) {
            database.treinoQueries.selectTreinoPorId(userId, id).executeAsOneOrNull()?.toDomain()
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

    override suspend fun getExercicios(treinoId: String): List<Exercicio> =
        withContext(Dispatchers.Default) {
            database.exercicioQueries.selectExerciciosDoTreino(treinoId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getExerciciosNaoSincronizados(userId: String): List<ExercicioDoTreino> =
        withContext(Dispatchers.Default) {
            database.exercicioQueries.selectNaoSincronizados(userId).executeAsList().map {
                ExercicioDoTreino(treinoId = it.treinoId, ordem = it.ordem.toInt(), exercicio = it.toDomain())
            }
        }

    override suspend fun salvarExercicios(
        userId: String,
        treinoId: String,
        exercicios: List<Exercicio>,
        sincronizado: Boolean
    ) {
        withContext(Dispatchers.Default) {
            database.exercicioQueries.transaction {
                exercicios.forEachIndexed { ordem, exercicio ->
                    database.exercicioQueries.upsertExercicio(
                        id = exercicio.id,
                        treinoId = treinoId,
                        userId = userId,
                        ordem = ordem.toLong(),
                        nome = exercicio.nome,
                        series = exercicio.seriesPlanejadas.toLong(),
                        reps = exercicio.repsPlanejadas.toLong(),
                        cargaKg = exercicio.cargaPlanejadaKg,
                        sincronizado = if (sincronizado) 1L else 0L
                    )
                }
            }
        }
    }

    override suspend fun marcarExercicioSincronizado(id: String) {
        withContext(Dispatchers.Default) {
            database.exercicioQueries.marcarSincronizado(id)
        }
    }

    override suspend fun getExecucao(treinoId: String, data: String): ExecucaoTreino? =
        withContext(Dispatchers.Default) {
            database.execucaoQueries.selectExecucao(treinoId, data).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getUltimaExecucao(treinoId: String): String? =
        withContext(Dispatchers.Default) {
            database.execucaoQueries.selectUltimaExecucao(treinoId).executeAsOneOrNull()?.ultima
        }

    override suspend fun getUltimasExecucoes(userId: String): Map<String, String> =
        withContext(Dispatchers.Default) {
            database.execucaoQueries.selectUltimasExecucoes(userId).executeAsList()
                .mapNotNull { linha -> linha.ultima?.let { linha.treinoId to it } }
                .toMap()
        }

    override suspend fun getExecucoesNaoSincronizadas(userId: String): List<ExecucaoTreino> =
        withContext(Dispatchers.Default) {
            database.execucaoQueries.selectNaoSincronizadas(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun salvarExecucao(userId: String, execucao: ExecucaoTreino, sincronizado: Boolean) {
        withContext(Dispatchers.Default) {
            database.execucaoQueries.upsertExecucao(
                treinoId = execucao.treinoId,
                userId = userId,
                dataExecucao = execucao.data,
                seriesJson = Json.encodeToString(execucao.series),
                sincronizado = if (sincronizado) 1L else 0L
            )
        }
    }

    override suspend fun marcarExecucaoSincronizada(treinoId: String, data: String) {
        withContext(Dispatchers.Default) {
            database.execucaoQueries.marcarSincronizada(treinoId, data)
        }
    }
}

private fun ExecucaoTreinoEntity.toDomain() = ExecucaoTreino(
    treinoId = treinoId,
    data = dataExecucao,
    series = Json.decodeFromString(seriesJson),
)

private fun ExercicioEntity.toDomain() = Exercicio(
    id = id,
    nome = nome,
    seriesPlanejadas = series.toInt(),
    repsPlanejadas = reps.toInt(),
    cargaPlanejadaKg = cargaKg,
)

private fun TreinoEntity.toDomain() = Treino(
    id = id,
    nome = nome,
    tipo = tipo,
    duracaoMin = duracaoMin?.toInt(),
    data = dataTreino,
    observacoes = observacoes
)