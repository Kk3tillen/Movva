package br.com.movva.components.treino.data

import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.Treino
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface TreinoRemoteDataSource {
    suspend fun inserir(userId: String, treino: Treino)
    suspend fun getTreinos(userId: String): List<Treino>
    suspend fun upsertExercicios(userId: String, exercicios: List<ExercicioDoTreino>)
    suspend fun getExercicios(treinoId: String): List<Exercicio>
    suspend fun upsertExecucao(userId: String, execucao: ExecucaoTreino)
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

    override suspend fun upsertExercicios(userId: String, exercicios: List<ExercicioDoTreino>) {
        if (exercicios.isEmpty()) return
        supabaseClient.from("treino_exercicios").upsert(
            exercicios.map {
                ExercicioDto(
                    id = it.exercicio.id,
                    treinoId = it.treinoId,
                    usuarioId = userId,
                    ordem = it.ordem,
                    nome = it.exercicio.nome,
                    series = it.exercicio.seriesPlanejadas,
                    reps = it.exercicio.repsPlanejadas,
                    cargaKg = it.exercicio.cargaPlanejadaKg
                )
            }
        )
    }

    override suspend fun getExercicios(treinoId: String): List<Exercicio> =
        supabaseClient.from("treino_exercicios")
            .select {
                filter { eq("treino_id", treinoId) }
                order("ordem", Order.ASCENDING)
            }
            .decodeList<ExercicioDto>()
            .map {
                Exercicio(
                    id = it.id,
                    nome = it.nome,
                    seriesPlanejadas = it.series,
                    repsPlanejadas = it.reps,
                    cargaPlanejadaKg = it.cargaKg
                )
            }

    override suspend fun upsertExecucao(userId: String, execucao: ExecucaoTreino) {
        supabaseClient.from("treino_execucoes").upsert(
            ExecucaoDto(
                treinoId = execucao.treinoId,
                usuarioId = userId,
                data = execucao.data,
                series = execucao.series
            )
        ) {
            // uma execução por treino por dia
            onConflict = "treino_id,data"
        }
    }
}

@Serializable
private data class ExecucaoDto(
    @SerialName("treino_id") val treinoId: String,
    @SerialName("usuario_id") val usuarioId: String,
    val data: String,
    val series: List<SerieRegistrada>,
)

@Serializable
private data class ExercicioDto(
    val id: String,
    @SerialName("treino_id") val treinoId: String,
    @SerialName("usuario_id") val usuarioId: String,
    val ordem: Int,
    val nome: String,
    val series: Int,
    val reps: Int,
    @SerialName("carga_kg") val cargaKg: Double? = null,
)

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