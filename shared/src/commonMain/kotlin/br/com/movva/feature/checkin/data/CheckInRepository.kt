package br.com.movva.feature.checkin.data

import br.com.movva.auth.AuthRepository
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.domain.CheckIn
import kotlinx.coroutines.CancellationException

interface CheckInRepository {
    suspend fun getCheckInDeHoje(): CheckIn?
    suspend fun registrarCheckIn(treinoFeito: Boolean, dietaSeguida: Boolean): Result<Unit>
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

    override suspend fun registrarCheckIn(treinoFeito: Boolean, dietaSeguida: Boolean): Result<Unit> = runCatching {
        val userId = authRepository.currentUser()?.id
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val checkIn = CheckIn(
            data = dataDeHojeIso(),
            treinoFeito = treinoFeito,
            dietaSeguida = dietaSeguida,
        )

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
