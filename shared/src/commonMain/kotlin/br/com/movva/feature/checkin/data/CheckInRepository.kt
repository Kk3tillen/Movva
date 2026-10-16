package br.com.movva.feature.checkin.data

import br.com.movva.auth.AuthRepository
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.core.util.diaAnteriorIso
import br.com.movva.feature.checkin.domain.CheckIn
import kotlinx.coroutines.CancellationException

// janela de dias usada tanto para sincronizar histórico recente quanto para limitar o cálculo da streak
private const val JANELA_STREAK_DIAS = 400

interface CheckInRepository {
    suspend fun getCheckInDeHoje(): CheckIn?
    suspend fun marcarTreinoFeito(data: String): Result<Unit>
    suspend fun marcarDietaSeguida(data: String): Result<Unit>
    suspend fun getStreakAtual(): Int
}

class CheckInRepositoryImpl(
    private val remoteDataSource: CheckInRemoteDataSource,
    private val localDataSource: CheckInLocalDataSource,
    private val authRepository: AuthRepository
) : CheckInRepository {

    override suspend fun getCheckInDeHoje(): CheckIn? {
        val userId = authRepository.currentUser()?.id ?: return null
        val hoje = dataDeHojeIso()

        val local = localDataSource.getCheckInDoDia(userId, hoje)

        try {
            enviarPendentes(userId)
            val remoto = remoteDataSource.getCheckInDoDia(userId, hoje)
            if (remoto != null) {
                localDataSource.salvar(userId, remoto, sincronizado = true)
                return remoto
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("CHECKIN_SYNC_ERRO: ${e.message}")
        }

        return local
    }

    override suspend fun getStreakAtual(): Int {
        val userId = authRepository.currentUser()?.id ?: return 0
        sincronizarRecentes(userId)

        var data = dataDeHojeIso()
        var checkIn = localDataSource.getCheckInDoDia(userId, data)

        // o dia de hoje só entra na contagem se já houve atividade; caso contrário
        // começa a contar de ontem para trás, sem considerar a streak quebrada
        if (checkIn?.temAtividade != true) {
            data = diaAnteriorIso(data)
            checkIn = localDataSource.getCheckInDoDia(userId, data)
        }

        var dias = 0
        while (checkIn?.temAtividade == true && dias < JANELA_STREAK_DIAS) {
            dias++
            data = diaAnteriorIso(data)
            checkIn = localDataSource.getCheckInDoDia(userId, data)
        }
        return dias
    }

    private suspend fun sincronizarRecentes(userId: String) {
        try {
            enviarPendentes(userId)
            val desde = dataMaisAntigaDaJanela()
            remoteDataSource.getRecentes(userId, desde).forEach { checkIn ->
                localDataSource.salvar(userId, checkIn, sincronizado = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("CHECKIN_SYNC_ERRO: ${e.message}")
        }
    }

    private fun dataMaisAntigaDaJanela(): String {
        var data = dataDeHojeIso()
        repeat(JANELA_STREAK_DIAS) { data = diaAnteriorIso(data) }
        return data
    }

    override suspend fun marcarTreinoFeito(data: String): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        // preserva a dieta já marcada para o dia, se houver
        val existente = localDataSource.getCheckInDoDia(userId, data)
        val checkIn = CheckIn(
            data = data,
            treinoFeito = true,
            dietaSeguida = existente?.dietaSeguida ?: false,
        )

        salvarESincronizar(userId, checkIn)
    }

    override suspend fun marcarDietaSeguida(data: String): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        // preserva o treino já marcado para o dia, se houver
        val existente = localDataSource.getCheckInDoDia(userId, data)
        val checkIn = CheckIn(
            data = data,
            treinoFeito = existente?.treinoFeito ?: false,
            dietaSeguida = true,
        )

        salvarESincronizar(userId, checkIn)
    }

    private suspend fun salvarESincronizar(userId: String, checkIn: CheckIn) {
        // 1) salva local primeiro: o check-in nunca se perde
        localDataSource.salvar(userId, checkIn, sincronizado = false)

        // 2) tenta enviar; se falhar, fica pendente e é reenviado na próxima carga
        try {
            remoteDataSource.upsert(userId, checkIn)
            localDataSource.marcarSincronizado(userId, checkIn.data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("CHECKIN_SYNC_ERRO: ${e.message}")
        }
    }

    private suspend fun enviarPendentes(userId: String) {
        localDataSource.getNaoSincronizados(userId).forEach { checkIn ->
            remoteDataSource.upsert(userId, checkIn)
            localDataSource.marcarSincronizado(userId, checkIn.data)
        }
    }
}
