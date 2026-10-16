package br.com.movva.components.treino.domain

data class Treino(
    val id: String,
    val nome: String,
    val tipo: String,
    val duracaoMin: Int?,
    val data: String,
    val observacoes: String?,
    // data (AAAA-MM-DD) da última vez em que o treino foi feito; null se nunca foi
    val ultimaExecucao: String? = null,
)