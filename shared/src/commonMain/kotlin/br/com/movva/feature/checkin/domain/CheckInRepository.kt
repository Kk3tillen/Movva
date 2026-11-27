package br.com.movva.feature.checkin.domain

interface CheckInRepository {
    suspend fun getCheckInDeHoje(): CheckIn?
    suspend fun marcarTreinoFeito(data: String): Result<Unit>
    suspend fun marcarDietaSeguida(data: String): Result<Unit>
    suspend fun getStreakAtual(): Int
}
