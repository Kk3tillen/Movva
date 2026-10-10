package br.com.movva.feature.treino.data

import br.com.movva.auth.AuthRepository
import br.com.movva.feature.treino.domain.Treino
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
        data: String,
        observacoes: String?
    ): Result<Unit>
}

class TreinoRepositoryImpl(
    private val remoteDataSource: TreinoRemoteDataSource,
    private val localDataSource: TreinoLocalDataSource,
    private val authRepository: AuthRepository
) : TreinoRepository {

    override fun getTreinos(): Flow<TreinosResultado> = flow {
        val userId = authRepository.currentUser()?.id
            ?: run {
                emit(TreinosResultado.Erro("Nenhum usuário autenticado"))
                return@flow
            }

        val cache = localDataSource.getTreinos(userId)
        emit(TreinosResultado.Sucesso(cache))

        try {
            enviarPendentes(userId)
            val remotos = remoteDataSource.getTreinos(userId)
            remotos.forEach { localDataSource.salvar(userId, it, sincronizado = true) }
            emit(TreinosResultado.Sucesso(localDataSource.getTreinos(userId)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cache.isEmpty()) {
                emit(TreinosResultado.Erro(e.message ?: "Não foi possível carregar os treinos"))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun salvar(
        nome: String,
        tipo: String,
        duracaoMin: Int?,
        data: String,
        observacoes: String?
    ): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val treino = Treino(
            id = Uuid.random().toString(),
            nome = nome,
            tipo = tipo,
            duracaoMin = duracaoMin,
            data = data,
            observacoes = observacoes
        )

        // 1) salva local primeiro: o treino nunca se perde
        localDataSource.salvar(userId, treino, sincronizado = false)

        // 2) tenta enviar; se falhar, fica pendente e é reenviado depois
        try {
            remoteDataSource.inserir(userId, treino)
            localDataSource.marcarSincronizado(treino.id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private suspend fun enviarPendentes(userId: String) {
        localDataSource.getNaoSincronizados(userId).forEach { treino ->
            remoteDataSource.inserir(userId, treino)
            localDataSource.marcarSincronizado(treino.id)
        }
    }
}