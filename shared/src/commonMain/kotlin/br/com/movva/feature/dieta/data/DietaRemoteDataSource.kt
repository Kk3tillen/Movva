package br.com.movva.feature.dieta.data

import br.com.movva.feature.dieta.domain.Dieta
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DietaRemoteDataSource {
    suspend fun inserir(userId: String, dieta: Dieta)
    suspend fun getDietas(userId: String): List<Dieta>
}

class DietaRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : DietaRemoteDataSource {

    override suspend fun inserir(userId: String, dieta: Dieta) {
        supabaseClient.from("dietas").insert(
            DietaDto(
                id = dieta.id,
                usuarioId = userId,
                tipoRefeicao = dieta.tipoRefeicao,
                descricao = dieta.descricao,
                data = dieta.data
            )
        )
    }

    override suspend fun getDietas(userId: String): List<Dieta> =
        supabaseClient.from("dietas")
            .select {
                filter { eq("usuario_id", userId) }
                order("data", Order.DESCENDING)
            }
            .decodeList<DietaDto>()
            .map {
                Dieta(
                    id = it.id,
                    tipoRefeicao = it.tipoRefeicao,
                    descricao = it.descricao,
                    data = it.data
                )
            }
}

@Serializable
private data class DietaDto(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("tipo_refeicao") val tipoRefeicao: String,
    val descricao: String,
    val data: String
)