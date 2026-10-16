package br.com.movva.components.dieta

import br.com.movva.components.dieta.data.DietaLocalDataSource
import br.com.movva.components.dieta.data.DietaRemoteDataSource
import br.com.movva.components.dieta.domain.Dieta

class FakeDietaLocalDataSource : DietaLocalDataSource {
    private val salvos = mutableMapOf<String, Dieta>()
    private val sincronizados = mutableSetOf<String>()

    override suspend fun getDietas(userId: String): List<Dieta> = salvos.values.toList()

    override suspend fun getNaoSincronizados(userId: String): List<Dieta> =
        salvos.values.filter { it.id !in sincronizados }

    override suspend fun salvar(userId: String, dieta: Dieta, sincronizado: Boolean) {
        salvos[dieta.id] = dieta
        if (sincronizado) sincronizados.add(dieta.id) else sincronizados.remove(dieta.id)
    }

    override suspend fun marcarSincronizado(id: String) {
        sincronizados.add(id)
    }
}

class FakeDietaRemoteDataSource(
    var falharAoEnviar: Boolean = false,
) : DietaRemoteDataSource {
    val inseridas = mutableListOf<Dieta>()

    override suspend fun inserir(userId: String, dieta: Dieta) {
        if (falharAoEnviar) throw RuntimeException("sem conexão")
        inseridas.add(dieta)
    }

    override suspend fun getDietas(userId: String): List<Dieta> = inseridas.toList()
}
