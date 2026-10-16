package br.com.movva.feature.checkin.data

import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.shared.db.CheckInEntity
import br.com.movva.shared.db.MovvaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CheckInLocalDataSource {
    suspend fun getCheckInDoDia(userId: String, data: String): CheckIn?
    suspend fun getNaoSincronizados(userId: String): List<CheckIn>
    suspend fun salvar(userId: String, checkIn: CheckIn, sincronizado: Boolean)
    suspend fun marcarSincronizado(userId: String, data: String)
}

class CheckInLocalDataSourceImpl(
    private val database: MovvaDatabase
) : CheckInLocalDataSource {

    override suspend fun getCheckInDoDia(userId: String, data: String): CheckIn? =
        withContext(Dispatchers.Default) {
            database.checkInQueries.selectCheckInDoDia(userId, data).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getNaoSincronizados(userId: String): List<CheckIn> =
        withContext(Dispatchers.Default) {
            database.checkInQueries.selectNaoSincronizados(userId).executeAsList().map { it.toDomain() }
        }

    override suspend fun salvar(userId: String, checkIn: CheckIn, sincronizado: Boolean) {
        withContext(Dispatchers.Default) {
            database.checkInQueries.upsertCheckIn(
                userId = userId,
                dataCheckIn = checkIn.data,
                treinoFeito = if (checkIn.treinoFeito) 1L else 0L,
                dietaSeguida = if (checkIn.dietaSeguida) 1L else 0L,
                sincronizado = if (sincronizado) 1L else 0L
            )
        }
    }

    override suspend fun marcarSincronizado(userId: String, data: String) {
        withContext(Dispatchers.Default) {
            database.checkInQueries.marcarSincronizado(userId, data)
        }
    }
}

private fun CheckInEntity.toDomain() = CheckIn(
    data = dataCheckIn,
    treinoFeito = treinoFeito == 1L,
    dietaSeguida = dietaSeguida == 1L,
)
