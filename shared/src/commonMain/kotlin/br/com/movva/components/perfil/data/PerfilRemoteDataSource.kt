package br.com.movva.components.perfil.data

import br.com.movva.components.perfil.domain.Perfil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface PerfilRemoteDataSource {
    suspend fun getPerfil(userId: String): Perfil
}

class PerfilRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : PerfilRemoteDataSource {

    override suspend fun getPerfil(userId: String): Perfil {
        val dto = supabaseClient.from("perfil_publico")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingle<PerfilDto>()
        return Perfil(nome = dto.nome, userTag = dto.userTag, avatarId = dto.avatarId.orEmpty())
    }
}

@Serializable
private data class PerfilDto(
    @SerialName("name") val nome: String,
    @SerialName("user_tag") val userTag: String,
    @SerialName("avatar_id") val avatarId: String? = null
)
