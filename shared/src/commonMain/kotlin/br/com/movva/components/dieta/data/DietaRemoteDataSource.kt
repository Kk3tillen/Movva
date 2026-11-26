package br.com.movva.components.dieta.data

import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.ItemRefeicao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DietaRemoteDataSource {
    suspend fun upsert(userId: String, dieta: Dieta)
    suspend fun getDietas(userId: String): List<Dieta>
}

class DietaRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : DietaRemoteDataSource {

    // upsert pelo id: a mesma refeição pode ser editada depois de enviada
    override suspend fun upsert(userId: String, dieta: Dieta) {
        supabaseClient.from("dietas").upsert(
            DietaDto(
                id = dieta.id,
                usuarioId = userId,
                tipoRefeicao = dieta.tipoRefeicao,
                descricao = dieta.descricao,
                data = dieta.data,
                itens = dieta.itens
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
                    data = it.data,
                    itens = it.itens
                )
            }
}

@Serializable
private data class DietaDto(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("tipo_refeicao") val tipoRefeicao: String,
    val descricao: String,
    val data: String,
    val itens: List<ItemRefeicao> = emptyList()
)