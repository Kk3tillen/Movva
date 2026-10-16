package br.com.movva.feature.checkin

import br.com.movva.auth.AuthRepository
import br.com.movva.auth.LoggedInUser
import br.com.movva.feature.checkin.data.CheckInLocalDataSource
import br.com.movva.feature.checkin.data.CheckInRemoteDataSource
import br.com.movva.feature.checkin.domain.CheckIn

class FakeAuthRepository(
    private var usuario: LoggedInUser? = LoggedInUser(id = "user-1", nome = "Teste", userTag = "@teste"),
) : AuthRepository {
    override suspend fun login(email: String, senha: String): Result<Unit> = Result.success(Unit)
    override suspend fun logout() {
        usuario = null
    }

    override suspend fun currentUser(): LoggedInUser? = usuario
}

class FakeCheckInLocalDataSource : CheckInLocalDataSource {
    private val salvos = mutableMapOf<Pair<String, String>, CheckIn>()
    private val sincronizados = mutableSetOf<Pair<String, String>>()

    override suspend fun getCheckInDoDia(userId: String, data: String): CheckIn? =
        salvos[userId to data]

    override suspend fun getNaoSincronizados(userId: String): List<CheckIn> =
        salvos.entries
            .filter { (chave, _) -> chave.first == userId && chave !in sincronizados }
            .map { it.value }

    override suspend fun salvar(userId: String, checkIn: CheckIn, sincronizado: Boolean) {
        val chave = userId to checkIn.data
        salvos[chave] = checkIn
        if (sincronizado) sincronizados.add(chave) else sincronizados.remove(chave)
    }

    override suspend fun marcarSincronizado(userId: String, data: String) {
        sincronizados.add(userId to data)
    }
}

class FakeCheckInRemoteDataSource(
    var falharAoEnviar: Boolean = false,
    var falharAoLer: Boolean = false,
) : CheckInRemoteDataSource {
    val enviados = mutableListOf<CheckIn>()
    private val remoto = mutableMapOf<Pair<String, String>, CheckIn>()

    fun semear(userId: String, checkIn: CheckIn) {
        remoto[userId to checkIn.data] = checkIn
    }

    override suspend fun upsert(userId: String, checkIn: CheckIn) {
        if (falharAoEnviar) throw RuntimeException("sem conexão")
        enviados.add(checkIn)
        remoto[userId to checkIn.data] = checkIn
    }

    override suspend fun getCheckInDoDia(userId: String, data: String): CheckIn? {
        if (falharAoLer) throw RuntimeException("sem conexão")
        return remoto[userId to data]
    }

    override suspend fun getRecentes(userId: String, desde: String): List<CheckIn> {
        if (falharAoLer) throw RuntimeException("sem conexão")
        return remoto.entries
            .filter { (chave, checkIn) -> chave.first == userId && checkIn.data >= desde }
            .map { it.value }
    }
}
