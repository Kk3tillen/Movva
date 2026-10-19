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

// Soma os totais só das refeições já marcadas como feitas hoje (usado na Dieta e no resumo da Home).
fun List<Dieta>.resumoDeHoje(): TotaisRefeicao =
    filter { it.feitaHoje }.fold(TotaisRefeicao()) { acc, dieta -> acc + dieta.totais }
