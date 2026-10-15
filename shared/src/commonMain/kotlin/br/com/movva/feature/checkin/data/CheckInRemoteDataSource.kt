package br.com.movva.feature.checkin.data

import br.com.movva.feature.checkin.domain.CheckIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CheckInRemoteDataSource {
    suspend fun upsert(userId: String, checkIn: CheckIn)
    suspend fun getCheckInDoDia(userId: String, data: String): CheckIn?
}

class CheckInRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : CheckInRemoteDataSource {

    override suspend fun upsert(userId: String, checkIn: CheckIn) {
        supabaseClient.from("check_ins").upsert(
            CheckInUpsertDto(
                usuarioId = userId,
                data = checkIn.data,
                treinoFeito = checkIn.treinoFeito,
                dietaSeguida = checkIn.dietaSeguida,
            )
        ) {
            // check_ins tem unique(usuario_id, data, grupo_id); grupo_id = null para check-in pessoal.
            onConflict = "usuario_id,data,grupo_id"
        }
    }

    override suspend fun getCheckInDoDia(userId: String, data: String): CheckIn? =
        supabaseClient.from("check_ins")
            .select {
                // Não filtra grupo_id: hoje só existe check-in pessoal (grupo_id = null).
                // Quando o check-in de grupo (MOV06-09) existir, adicionar "grupo_id IS NULL" aqui.
                filter {
                    eq("usuario_id", userId)
                    eq("data", data)
                }
            }
            .decodeSingleOrNull<CheckInDto>()
            ?.let {
                CheckIn(
                    data = it.data,
                    treinoFeito = it.treinoFeito,
                    dietaSeguida = it.dietaSeguida,
                )
            }
}

@Serializable
private data class CheckInUpsertDto(
    @SerialName("usuario_id") val usuarioId: String,
    val data: String,
    @SerialName("grupo_id") val grupoId: String? = null,
    @SerialName("treino_feito") val treinoFeito: Boolean,
    @SerialName("dieta_seguida") val dietaSeguida: Boolean,
)

@Serializable
private data class CheckInDto(
    val data: String,
    @SerialName("treino_feito") val treinoFeito: Boolean,
    @SerialName("dieta_seguida") val dietaSeguida: Boolean,
)
