package br.com.movva.feature.treino.data

import br.com.movva.feature.treino.domain.Treino
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface TreinoRemoteDataSource {
    suspend fun inserir(userId: String, treino: Treino)
    suspend fun getTreinos(userId: String): List<Treino>
}

class TreinoRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : TreinoRemoteDataSource {

    override suspend fun inserir(userId: String, treino: Treino) {
        supabaseClient.from("treinos").insert(
            TreinoDto(
                id = treino.id,
                usuarioId = userId,
                nome = treino.nome,
                tipo = treino.tipo,
                duracaoMin = treino.duracaoMin,
                data = treino.data,
                observacoes = treino.observacoes
            )
        )
    }

    override suspend fun getTreinos(userId: String): List<Treino> =
        supabaseClient.from("treinos")
            .select {
                filter { eq("usuario_id", userId) }
                order("data", Order.DESCENDING)
            }
            .decodeList<TreinoDto>()
            .map {
                Treino(
                    id = it.id,
                    nome = it.nome,
                    tipo = it.tipo,
                    duracaoMin = it.duracaoMin,
                    data = it.data,
                    observacoes = it.observacoes
                )
            }
}

@Serializable
private data class TreinoDto(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    val nome: String,
    val tipo: String,
    @SerialName("duracao_min") val duracaoMin: Int? = null,
    val data: String,
    val observacoes: String? = null
)