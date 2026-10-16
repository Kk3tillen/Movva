package br.com.movva.components.treino.data

import br.com.movva.auth.AuthRepository
import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.Serie
import br.com.movva.components.treino.domain.Treino
import br.com.movva.components.treino.domain.TreinoDetalhe
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.data.CheckInRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

class TreinoRepositoryImpl(
    private val remoteDataSource: TreinoRemoteDataSource,
    private val localDataSource: TreinoLocalDataSource,
    private val authRepository: AuthRepository,
    private val checkInRepository: CheckInRepository
) : TreinoRepository {

    override fun getTreinos(): Flow<TreinosResultado> = flow {
        val userId = authRepository.currentUser()?.id
            ?: run {
                emit(TreinosResultado.Erro("Nenhum usuário autenticado"))
                return@flow
            }

        val cache = treinosComExecucoes(userId)
        emit(TreinosResultado.Sucesso(cache))

        try {
            enviarPendentes(userId)
            val remotos = remoteDataSource.getTreinos(userId)
            remotos.forEach { localDataSource.salvar(userId, it, sincronizado = true) }
            emit(TreinosResultado.Sucesso(treinosComExecucoes(userId)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cache.isEmpty()) {
                emit(TreinosResultado.Erro(e.message ?: "Não foi possível carregar os treinos"))
            }
        }
    }

    private suspend fun treinosComExecucoes(userId: String): List<Treino> {
        val ultimas = localDataSource.getUltimasExecucoes(userId)
        return localDataSource.getTreinos(userId).map { it.copy(ultimaExecucao = ultimas[it.id]) }
    }

     @OptIn(ExperimentalUuidApi::class)
    override suspend fun salvar(
        nome: String,
        tipo: String,
        duracaoMin: Int?,
        observacoes: String?,
        exercicios: List<Exercicio>
    ): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val treino = Treino(
            id = Uuid.random().toString(),
            nome = nome,
            tipo = tipo,
            duracaoMin = duracaoMin,
            data = dataDeHojeIso(),
            observacoes = observacoes
        )

        // 1) salva local primeiro: o treino nunca se perde
        localDataSource.salvar(userId, treino, sincronizado = false)
        localDataSource.salvarExercicios(userId, treino.id, exercicios, sincronizado = false)

        // 2) tenta enviar; se falhar, fica pendente e é reenviado depois
        try {
            remoteDataSource.inserir(userId, treino)
            localDataSource.marcarSincronizado(treino.id)
            enviarExerciciosPendentes(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("TREINO_SYNC_ERRO: ${e.message}")
        }
    }

    override suspend fun getTreinoDetalhe(id: String): Result<TreinoDetalhe> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")
        val treino = localDataSource.getTreino(userId, id)
            ?: throw IllegalStateException("Treino não encontrado")
        val hoje = dataDeHojeIso()
        val execucaoDeHoje = localDataSource.getExecucao(treino.id, hoje)
        val exercicios = getExercicios(userId, treino.id).map { exercicio ->
            val series = execucaoDeHoje?.series.orEmpty()
                .filter { it.exercicioId == exercicio.id }
                .map { Serie(it.numero, it.reps, it.cargaKg, it.concluida) }
            exercicio.copy(series = series)
        }

        TreinoDetalhe(
            id = treino.id,
            nome = treino.nome,
            rotulo = treino.tipo.uppercase(),
            duracaoEstimadaMin = treino.duracaoMin,
            exercicios = exercicios,
            ultimaExecucao = localDataSource.getUltimaExecucao(treino.id),
            feitoHoje = execucaoDeHoje != null,
        )
    }

    private suspend fun getExercicios(userId: String, treinoId: String): List<Exercicio> {
        val locais = localDataSource.getExercicios(treinoId)
        if (locais.isNotEmpty()) return locais

        return try {
            remoteDataSource.getExercicios(treinoId).also {
                localDataSource.salvarExercicios(userId, treinoId, it, sincronizado = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("EXERCICIO_SYNC_ERRO: ${e.message}")
            emptyList()
        }
    }

     override suspend fun finalizarTreino(
        treinoId: String,
        exercicios: List<Exercicio>
    ): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val execucao = ExecucaoTreino(
            treinoId = treinoId,
            data = dataDeHojeIso(),
            series = exercicios.flatMap { exercicio ->
                exercicio.series.map {
                    SerieRegistrada(exercicio.id, it.numero, it.reps, it.cargaKg, it.concluida)
                }
            }
        )
        localDataSource.salvarExecucao(userId, execucao, sincronizado = false)

        try {
            enviarExecucoesPendentes(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("EXECUCAO_SYNC_ERRO: ${e.message}")
        }

        checkInRepository.marcarTreinoFeito(execucao.data).getOrThrow()
    }

    private suspend fun enviarPendentes(userId: String) {
        localDataSource.getNaoSincronizados(userId).forEach { treino ->
            remoteDataSource.inserir(userId, treino)
            localDataSource.marcarSincronizado(treino.id)
        }
        try {
            enviarExerciciosPendentes(userId)
            enviarExecucoesPendentes(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // falha nos exercícios/execuções não impede a sincronização da lista de treinos
            println("EXERCICIO_SYNC_ERRO: ${e.message}")
        }
    }

    private suspend fun enviarExerciciosPendentes(userId: String) {
        val treinosPendentes = localDataSource.getNaoSincronizados(userId).map { it.id }.toSet()
        val pendentes = localDataSource.getExerciciosNaoSincronizados(userId)
            .filter { it.treinoId !in treinosPendentes }
        if (pendentes.isEmpty()) return

        remoteDataSource.upsertExercicios(userId, pendentes)
        pendentes.forEach { localDataSource.marcarExercicioSincronizado(it.exercicio.id) }
    }

    private suspend fun enviarExecucoesPendentes(userId: String) {
        val treinosPendentes = localDataSource.getNaoSincronizados(userId).map { it.id }.toSet()
        localDataSource.getExecucoesNaoSincronizadas(userId)
            .filter { it.treinoId !in treinosPendentes }
            .forEach { execucao ->
                remoteDataSource.upsertExecucao(userId, execucao)
                localDataSource.marcarExecucaoSincronizada(execucao.treinoId, execucao.data)
            }
    }
}
