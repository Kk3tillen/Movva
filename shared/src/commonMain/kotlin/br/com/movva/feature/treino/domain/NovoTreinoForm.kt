package br.com.movva.feature.treino.domain

data class NovoTreinoForm(
    val nome: String = "",
    val tipo: String = "",
    val duracao: String = "",
    val data: String = "",
    val observacoes: String = "",
)

data class NovoTreinoErros(
    val nome: String? = null,
    val tipo: String? = null,
    val duracao: String? = null,
    val data: String? = null,
) {
    val temErro: Boolean
        get() = nome != null || tipo != null || duracao != null || data != null
}

fun validarNovoTreino(form: NovoTreinoForm): NovoTreinoErros = NovoTreinoErros(
    nome = if (form.nome.isBlank()) "Informe o nome do treino" else null,
    tipo = if (form.tipo.isBlank()) "Informe o tipo do treino" else null,
    duracao = validarDuracao(form.duracao),
    data = validarData(form.data),
)

private fun validarDuracao(texto: String): String? {
    if (texto.isBlank()) return null // opcional
    val valor = texto.toIntOrNull() ?: return "Use apenas números"
    return if (valor <= 0) "A duração deve ser maior que zero" else null
}

private fun validarData(texto: String): String? {
    val partes = texto.split("-")
    if (partes.size != 3) return "Use o formato AAAA-MM-DD"

    val ano = partes[0].takeIf { it.length == 4 }?.toIntOrNull()
    val mes = partes[1].takeIf { it.length == 2 }?.toIntOrNull()
    val dia = partes[2].takeIf { it.length == 2 }?.toIntOrNull()
    if (ano == null || mes == null || dia == null) return "Use o formato AAAA-MM-DD"

    if (mes !in 1..12) return "Mês inválido"
    val diasNoMes = when (mes) {
        2 -> if (ehBissexto(ano)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return if (dia !in 1..diasNoMes) "Dia inválido" else null
}

private fun ehBissexto(ano: Int) = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0