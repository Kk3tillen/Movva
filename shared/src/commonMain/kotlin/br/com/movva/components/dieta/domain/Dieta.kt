package br.com.movva.components.dieta.domain

data class Dieta(
    val id: String,
    val tipoRefeicao: String,
    val descricao: String,
    val data: String,
    val itens: List<ItemRefeicao> = emptyList(),
    val feitaHoje: Boolean = false,
    val ultimaVezFeita: String? = null,
) {
    val totais: TotaisRefeicao get() = itens.totais()
}
