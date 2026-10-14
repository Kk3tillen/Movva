package br.com.movva.feature.dieta.domain

data class Dieta(
    val id: String,
    val tipoRefeicao: String,
    val descricao: String,
    val data: String,
)