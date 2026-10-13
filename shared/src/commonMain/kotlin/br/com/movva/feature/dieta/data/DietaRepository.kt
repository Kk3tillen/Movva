package br.com.movva.feature.dieta.data

import br.com.movva.auth.AuthRepository
import br.com.movva.feature.dieta.domain.Dieta
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface DietasResultado {
    data class Sucesso(val dietas: List<Dieta>) : DietasResultado
    data class Erro(val mensagem: String) : DietasResultado
}

interface DietaRepository {
    fun getDietas(): Flow<DietasResultado>
    suspend fun salvar(
        tipoRefeicao: String,
        descricao: String,
        data: String
    ): Result<Unit>
}

class DietaRepositoryImpl(
    private val remoteDataSource: DietaRemoteDataSource,
    private val localDataSource: DietaLocalDataSource,
    private val authRepository: AuthRepository
) : DietaRepository {

    override fun getDietas(): Flow<DietasResultado> = flow {
        val userId = authRepository.currentUser()?.id
            ?: run {
                emit(DietasResultado.Erro("Nenhum usuário autenticado"))
                return@flow
            }

        val cache = localDataSource.getDietas(userId)
        emit(DietasResultado.Sucesso(cache))

        try {
            enviarPendentes(userId)
            val remotas = remoteDataSource.getDietas(userId)
            remotas.forEach { localDataSource.salvar(userId, it, sincronizado = true) }
            emit(DietasResultado.Sucesso(localDataSource.getDietas(userId)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cache.isEmpty()) {
                emit(DietasResultado.Erro(e.message ?: "Não foi possível carregar as refeições"))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun salvar(
        tipoRefeicao: String,
        descricao: String,
        data: String
    ): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val dieta = Dieta(
            id = Uuid.random().toString(),
            tipoRefeicao = tipoRefeicao,
            descricao = descricao,
            data = data
        )

        // 1) salva local primeiro: a refeição nunca se perde
        localDataSource.salvar(userId, dieta, sincronizado = false)

        // 2) tenta enviar; se falhar, fica pendente e é reenviada depois
        try {
            remoteDataSource.inserir(userId, dieta)
            localDataSource.marcarSincronizado(dieta.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("DIETA_SYNC_ERRO: ${e.message}")
        }
    }

    private suspend fun enviarPendentes(userId: String) {
        localDataSource.getNaoSincronizados(userId).forEach { dieta ->
            remoteDataSource.inserir(userId, dieta)
            localDataSource.marcarSincronizado(dieta.id)
        }
    }
}