package br.com.movva.components.treino.domain

data class TreinoDetalhe(
    val id: String,
    val nome: String,
    val rotulo: String,
    val duracaoEstimadaMin: Int?,
    val exercicios: List<Exercicio>,
    val ultimaExecucao: String? = null,
    // quando true, exercicios[].series traz o que foi registrado hoje
    val feitoHoje: Boolean = false,
)

data class Exercicio(
    val id: String,
    val nome: String,
    val seriesPlanejadas: Int,
    val repsPlanejadas: Int,
    val cargaPlanejadaKg: Double?,
    val series: List<Serie> = emptyList(),
)

data class Serie(
    val numero: Int,
    val reps: Int,
    val cargaKg: Double?,
    val concluida: Boolean,
)

// "2026-10-09" -> "09/10/2026". Se o formato for outro, mostra como veio.
fun formatarData(data: String): String {
    val partes = data.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else data
}

// 40.0 -> "40", 42.5 -> "42.5", null -> ""
fun formatarCarga(cargaKg: Double?): String = when {
    cargaKg == null -> ""
    cargaKg % 1.0 == 0.0 -> cargaKg.toLong().toString()
    else -> cargaKg.toString()
}

// "4 séries × 10 reps · 40kg"
fun Exercicio.resumo(): String = listOfNotNull(
    "$seriesPlanejadas ${if (seriesPlanejadas == 1) "série" else "séries"} × $repsPlanejadas reps",
    cargaPlanejadaKg?.let { "${formatarCarga(it)}kg" },
).joinToString(" · ")
