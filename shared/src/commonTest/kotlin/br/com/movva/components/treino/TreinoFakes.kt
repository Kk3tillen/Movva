package br.com.movva.components.treino

import br.com.movva.components.treino.data.TreinoLocalDataSource
import br.com.movva.components.treino.data.TreinoRemoteDataSource
import br.com.movva.components.treino.domain.Treino

class FakeTreinoLocalDataSource : TreinoLocalDataSource {
    private val salvos = mutableMapOf<String, Treino>()
    private val sincronizados = mutableSetOf<String>()

    override suspend fun getTreinos(userId: String): List<Treino> = salvos.values.toList()

    override suspend fun getNaoSincronizados(userId: String): List<Treino> =
        salvos.values.filter { it.id !in sincronizados }

    override suspend fun salvar(userId: String, treino: Treino, sincronizado: Boolean) {
        salvos[treino.id] = treino
        if (sincronizado) sincronizados.add(treino.id) else sincronizados.remove(treino.id)
    }

    override suspend fun marcarSincronizado(id: String) {
        sincronizados.add(id)
    }
}

class FakeTreinoRemoteDataSource(
    var falharAoEnviar: Boolean = false,
) : TreinoRemoteDataSource {
    val inseridos = mutableListOf<Treino>()

    override suspend fun inserir(userId: String, treino: Treino) {
        if (falharAoEnviar) throw RuntimeException("sem conexão")
        inseridos.add(treino)
    }

    override suspend fun getTreinos(userId: String): List<Treino> = inseridos.toList()
}
