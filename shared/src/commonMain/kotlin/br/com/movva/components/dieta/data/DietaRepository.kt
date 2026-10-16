package br.com.movva.components.dieta.data

import br.com.movva.auth.AuthRepository
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.ItemRefeicao
import br.com.movva.components.dieta.domain.descricao
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.data.CheckInRepository
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

class DietaRepositoryImpl(
    private val remoteDataSource: DietaRemoteDataSource,
    private val localDataSource: DietaLocalDataSource,
    private val authRepository: AuthRepository,
    private val checkInRepository: CheckInRepository
) : DietaRepository {

    override fun getDietas(): Flow<DietasResultado> = flow {
        val userId = authRepository.currentUser()?.id
            ?: run {
                emit(DietasResultado.Erro("Nenhum usuário autenticado"))
                return@flow
            }

        val cache = dietasComFeitas(userId)
        emit(DietasResultado.Sucesso(cache))

        try {
            enviarPendentes(userId)
            val remotas = remoteDataSource.getDietas(userId)
            remotas.forEach { localDataSource.salvar(userId, it, sincronizado = true) }
            emit(DietasResultado.Sucesso(dietasComFeitas(userId)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cache.isEmpty()) {
                emit(DietasResultado.Erro(e.message ?: "Não foi possível carregar as refeições"))
            }
        }
    }

    private suspend fun dietasComFeitas(userId: String): List<Dieta> {
        val hoje = dataDeHojeIso()
        val ultimas = localDataSource.getUltimasFeitas(userId)
        return localDataSource.getDietas(userId).map {
            val ultima = ultimas[it.id]
            it.copy(feitaHoje = ultima == hoje, ultimaVezFeita = ultima)
        }
    }

    override suspend fun marcarFeitaHoje(dietaId: String, feita: Boolean): Result<Unit> = runCatching {
        val userId = usuarioId()
        val marcacao = RefeicaoFeita(dietaId = dietaId, data = dataDeHojeIso(), feita = feita)
        localDataSource.salvarFeita(userId, marcacao, sincronizado = false)

        try {
            enviarFeitasPendentes(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("REFEICAO_FEITA_SYNC_ERRO: ${e.message}")
        }

        if (feita) checkInRepository.marcarDietaSeguida(marcacao.data).getOrThrow()
    }

    override suspend fun getDieta(id: String): Result<Dieta> = runCatching {
        localDataSource.getDieta(usuarioId(), id) ?: throw IllegalStateException("Refeição não encontrada")
    }

    override suspend fun getRefeicaoDeHoje(tipoRefeicao: String): Dieta? {
        val userId = authRepository.currentUser()?.id ?: return null
        return localDataSource.getDietasDoDia(userId, dataDeHojeIso())
            .firstOrNull { it.tipoRefeicao == tipoRefeicao }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun salvarRefeicao(
        id: String?,
        tipoRefeicao: String,
        itens: List<ItemRefeicao>
    ): Result<Dieta> = runCatching {
        val userId = usuarioId()
        val existente = id?.let { localDataSource.getDieta(userId, it) }

        val dieta = Dieta(
            id = existente?.id ?: Uuid.random().toString(),
            tipoRefeicao = tipoRefeicao,
            descricao = itens.descricao(),
            data = existente?.data ?: dataDeHojeIso(),
            itens = itens
        )

        // 1) salva local primeiro: a refeição nunca se perde
        localDataSource.salvar(userId, dieta, sincronizado = false)

        // 2) tenta enviar; se falhar, fica pendente e é reenviada depois
        try {
            remoteDataSource.upsert(userId, dieta)
            localDataSource.marcarSincronizado(dieta.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("DIETA_SYNC_ERRO: ${e.message}")
        }

        // registrar não marca check-in: isso acontece ao marcar a refeição como feita
        dieta
    }

    override suspend fun getAlimentosFrequentes(limite: Int): List<String> {
        val userId = authRepository.currentUser()?.id ?: return emptyList()
        val itens = localDataSource.getDietas(userId).flatMap { it.itens }
        val contagem = itens.groupingBy { it.alimentoId }.eachCount()
        return itens.map { it.alimentoId }
            .distinct()
            .sortedByDescending { contagem[it] ?: 0 }
            .take(limite)
    }

    private suspend fun usuarioId(): String =
        authRepository.currentUser()?.id ?: throw IllegalStateException("Nenhum usuário autenticado")

    private suspend fun enviarPendentes(userId: String) {
        localDataSource.getNaoSincronizados(userId).forEach { dieta ->
            remoteDataSource.upsert(userId, dieta)
            localDataSource.marcarSincronizado(dieta.id)
        }
        try {
            enviarFeitasPendentes(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("REFEICAO_FEITA_SYNC_ERRO: ${e.message}")
        }
    }

    private suspend fun enviarFeitasPendentes(userId: String) {
        val dietasPendentes = localDataSource.getNaoSincronizados(userId).map { it.id }.toSet()
        localDataSource.getFeitasNaoSincronizadas(userId)
            .filter { it.dietaId !in dietasPendentes }
            .forEach { marcacao ->
                remoteDataSource.upsertFeita(userId, marcacao)
                localDataSource.marcarFeitaSincronizada(marcacao.dietaId, marcacao.data)
            }
    }
}
