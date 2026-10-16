package br.com.movva.components.dieta.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

// O título é o que fica salvo em Dieta.tipoRefeicao (compatível com as refeições antigas, em texto livre).
enum class TipoRefeicao(val titulo: String) {
    CAFE("Café da manhã"),
    ALMOCO("Almoço"),
    LANCHE("Lanche"),
    JANTAR("Jantar");

    companion object {
        fun doTitulo(titulo: String): TipoRefeicao? =
            entries.firstOrNull { normalizar(it.titulo) == normalizar(titulo) }

        // antes das 10h café; 11h–14h almoço; a partir das 18h jantar; nos intervalos, lanche
        fun peloHorario(hora: Int): TipoRefeicao = when (hora) {
            in 0..9 -> CAFE
            in 11..14 -> ALMOCO
            in 18..23 -> JANTAR
            else -> LANCHE
        }
    }
}

data class Porcao(
    val nome: String,
    val nomePlural: String,
    val gramas: Double,
)

data class Alimento(
    val id: String,
    val nome: String,
    val icone: String,
    val porcoes: List<Porcao>,
    val kcalPor100g: Double,
    val proteinaPor100g: Double,
    val carboPor100g: Double,
    val gorduraPor100g: Double,
) {
    // porcao == null significa quantidade em gramas
    fun item(quantidade: Double, porcao: Porcao?): ItemRefeicao {
        val gramas = if (porcao == null) quantidade else quantidade * porcao.gramas
        val fator = gramas / 100.0
        return ItemRefeicao(
            alimentoId = id,
            nome = nome,
            icone = icone,
            quantidadeG = gramas,
            porcaoNome = porcao?.nome,
            quantidadePorcao = if (porcao == null) null else quantidade,
            kcal = kcalPor100g * fator,
            proteinaG = proteinaPor100g * fator,
            carboG = carboPor100g * fator,
            gorduraG = gorduraPor100g * fator,
        )
    }

    fun porcao(nome: String?): Porcao? = porcoes.firstOrNull { it.nome == nome }
}

// Salvo como JSON (itensJson no SQLite, coluna jsonb no Supabase).
@Serializable
data class ItemRefeicao(
    @SerialName("alimento_id") val alimentoId: String,
    val nome: String,
    val icone: String,
    @SerialName("quantidade_g") val quantidadeG: Double,
    @SerialName("porcao_nome") val porcaoNome: String? = null,
    @SerialName("quantidade_porcao") val quantidadePorcao: Double? = null,
    val kcal: Double,
    @SerialName("proteina_g") val proteinaG: Double,
    @SerialName("carbo_g") val carboG: Double,
    @SerialName("gordura_g") val gorduraG: Double,
) {
    // "150g · 1 xícara" ou só "150g"
    val porcaoDescricao: String
        get() {
            val gramas = "${formatarNumero(quantidadeG)}g"
            val qtd = quantidadePorcao ?: return gramas
            val nome = porcaoNome ?: return gramas
            val plural = CatalogoAlimentos.porId(alimentoId)?.porcao(nome)?.nomePlural ?: nome
            // em português o plural começa em 2 ("1,5 xícara", "2 xícaras")
            return "$gramas · ${formatarNumero(qtd)} ${if (qtd >= 2.0) plural else nome}"
        }
}

data class TotaisRefeicao(
    val kcal: Double = 0.0,
    val proteinaG: Double = 0.0,
    val carboG: Double = 0.0,
    val gorduraG: Double = 0.0,
) {
    operator fun plus(outro: TotaisRefeicao) = TotaisRefeicao(
        kcal + outro.kcal,
        proteinaG + outro.proteinaG,
        carboG + outro.carboG,
        gorduraG + outro.gorduraG,
    )
}

fun List<ItemRefeicao>.totais(): TotaisRefeicao = TotaisRefeicao(
    kcal = sumOf { it.kcal },
    proteinaG = sumOf { it.proteinaG },
    carboG = sumOf { it.carboG },
    gorduraG = sumOf { it.gorduraG },
)

// Descrição em texto gerada a partir dos itens (mantém compatibilidade com a coluna descricao).
fun List<ItemRefeicao>.descricao(): String = joinToString(", ") { it.nome }

// 150.0 -> "150", 1.5 -> "1,5"
fun formatarNumero(valor: Double): String {
    val arredondado = (valor * 10).roundToInt() / 10.0
    return if (arredondado % 1.0 == 0.0) {
        arredondado.toLong().toString()
    } else {
        arredondado.toString().replace('.', ',')
    }
}

// Aceita vírgula ou ponto: "1,5" -> 1.5
fun String.paraQuantidade(): Double? = trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

// minúsculas e sem acentos, para a busca
fun normalizar(texto: String): String = texto.lowercase().map {
    when (it) {
        'á', 'à', 'â', 'ã', 'ä' -> 'a'
        'é', 'è', 'ê', 'ë' -> 'e'
        'í', 'ì', 'î', 'ï' -> 'i'
        'ó', 'ò', 'ô', 'õ', 'ö' -> 'o'
        'ú', 'ù', 'û', 'ü' -> 'u'
        'ç' -> 'c'
        else -> it
    }
}.joinToString("").trim()
