package br.com.movva.components.perfil.data

import br.com.movva.auth.domain.AuthRepository
import br.com.movva.components.perfil.domain.PerfilRepository
import br.com.movva.components.perfil.domain.PerfilResultado
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PerfilRepositoryImpl(
    private val remoteDataSource: PerfilRemoteDataSource,
    private val localDataSource: PerfilLocalDataSource,
    private val authRepository: AuthRepository
) : PerfilRepository {

    override fun getPerfil(): Flow<PerfilResultado> = flow {
        val userId = authRepository.currentUser()?.id
            ?: run {
                emit(PerfilResultado.Erro("Nenhum usuário autenticado"))
                return@flow
            }

        val cache = localDataSource.getPerfil(userId)
        if (cache != null) emit(PerfilResultado.Sucesso(cache))

        try {
            val remoto = remoteDataSource.getPerfil(userId)
            localDataSource.salvarPerfil(userId, remoto)
            emit(PerfilResultado.Sucesso(remoto))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cache == null) {
                emit(PerfilResultado.Erro(e.message ?: "Não foi possível carregar o perfil"))
            }
        }
    }
}
