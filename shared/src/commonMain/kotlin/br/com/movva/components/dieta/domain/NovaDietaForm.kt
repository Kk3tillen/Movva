package br.com.movva.components.dieta.domain

import br.com.movva.components.treino.domain.validarData

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
    descricao = if (form.descricao.isBlank()) "Descreva o que você comeu" else null,
    data = validarData(form.data),
)