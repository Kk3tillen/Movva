package br.com.movva.feature.checkin.domain

data class CheckIn(
    val data: String,
    val treinoFeito: Boolean,
    val dietaSeguida: Boolean,
) {
    // dia conta pra streak se treino OU dieta foram registrados
    val temAtividade: Boolean get() = treinoFeito || dietaSeguida
}
