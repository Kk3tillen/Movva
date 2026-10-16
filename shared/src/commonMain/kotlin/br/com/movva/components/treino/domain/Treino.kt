package br.com.movva.components.treino.domain

data class Treino(
    val id: String,
    val nome: String,
    val tipo: String,
    val duracaoMin: Int?,
    val data: String,
    val observacoes: String?,
)