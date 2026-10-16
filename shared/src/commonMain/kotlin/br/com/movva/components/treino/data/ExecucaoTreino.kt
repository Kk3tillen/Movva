package br.com.movva.components.treino.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Registro de uma vez em que o treino foi feito (um por treino por dia).
data class ExecucaoTreino(
    val treinoId: String,
    val data: String,
    val series: List<SerieRegistrada>,
)

// Guardada como JSON (seriesJson no SQLite, coluna jsonb no Supabase).
@Serializable
data class SerieRegistrada(
    @SerialName("exercicio_id") val exercicioId: String,
    val numero: Int,
    val reps: Int,
    @SerialName("carga_kg") val cargaKg: Double?,
    val concluida: Boolean,
)
