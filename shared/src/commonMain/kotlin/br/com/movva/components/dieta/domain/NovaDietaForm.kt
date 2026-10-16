package br.com.movva.components.dieta.domain

data class NovaDietaForm(
    val tipoRefeicao: String = "",
    val descricao: String = "",
    val data: String = "",
)

data class NovaDietaErros(
    val tipoRefeicao: String? = null,
    val descricao: String? = null,
    val data: String? = null,
) {
    val temErro: Boolean
        get() = tipoRefeicao != null || descricao != null || data != null
}

fun validarNovaDieta(form: NovaDietaForm): NovaDietaErros = NovaDietaErros(
    tipoRefeicao = if (form.tipoRefeicao.isBlank()) "Informe o tipo da refeição" else null,
    descricao = if (form.descricao.isBlank()) "Informe a descrição" else null,
    data = validarData(form.data),
)

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
