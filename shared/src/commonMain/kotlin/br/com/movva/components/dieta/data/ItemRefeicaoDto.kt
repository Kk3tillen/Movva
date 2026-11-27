package br.com.movva.components.dieta.data

import br.com.movva.components.dieta.domain.ItemRefeicao
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO de serialização de [ItemRefeicao] para JSON local (SQLDelight) e Supabase. */
@Serializable
data class ItemRefeicaoDto(
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
    fun toDomain() = ItemRefeicao(
        alimentoId = alimentoId,
        nome = nome,
        icone = icone,
        quantidadeG = quantidadeG,
        porcaoNome = porcaoNome,
        quantidadePorcao = quantidadePorcao,
        kcal = kcal,
        proteinaG = proteinaG,
        carboG = carboG,
        gorduraG = gorduraG,
    )
}

fun ItemRefeicao.toDto() = ItemRefeicaoDto(
    alimentoId = alimentoId,
    nome = nome,
    icone = icone,
    quantidadeG = quantidadeG,
    porcaoNome = porcaoNome,
    quantidadePorcao = quantidadePorcao,
    kcal = kcal,
    proteinaG = proteinaG,
    carboG = carboG,
    gorduraG = gorduraG,
)
