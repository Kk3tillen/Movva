package br.com.movva.components.perfil.data

import br.com.movva.components.perfil.domain.Perfil
import br.com.movva.shared.db.MovvaDatabase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PerfilLocalDataSource {
    suspend fun getPerfil(userId: String): Perfil?
    suspend fun salvarPerfil(userId: String, perfil: Perfil)
}

class PerfilLocalDataSourceImpl(
    private val database: MovvaDatabase
) : PerfilLocalDataSource {

    override suspend fun getPerfil(userId: String): Perfil? = withContext(Dispatchers.Default) {
        database.perfilQueries.selectPerfil(userId).executeAsOneOrNull()?.let {
            Perfil(nome = it.nome, userTag = it.userTag, avatarId = it.avatarId)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun salvarPerfil(userId: String, perfil: Perfil) {
        withContext(Dispatchers.Default) {
            database.perfilQueries.upsertPerfil(
                userId = userId,
                nome = perfil.nome,
                userTag = perfil.userTag,
                atualizadoEm = Clock.System.now().toEpochMilliseconds(),
                avatarId = perfil.avatarId
            )
        }
    }
}
