package br.com.movva.feature.home.domain

import br.com.movva.core.util.diasNoMes
import br.com.movva.feature.checkin.domain.CheckIn

enum class StatusDiaCheckIn {
    NENHUM,
    TREINO,
    DIETA,
    AMBOS,
}

data class DiaCalendario(
    val dia: Int,
    val dataIso: String,
    val ehHoje: Boolean,
    val status: StatusDiaCheckIn,
    val descricaoAcessivel: String,
)

data class CalendarioMes(
    val ano: Int,
    val mes: Int,
    val nomeMes: String,
    val diaSemanaInicio: Int, // 0 = Domingo, 1 = Segunda, ..., 6 = Sábado
    val dias: List<DiaCalendario>,
) {
    val temRegistros: Boolean get() = dias.any { it.status != StatusDiaCheckIn.NENHUM }
}

fun montarCalendarioMes(
    ano: Int,
    mes: Int,
    hojeIso: String,
    checkIns: List<CheckIn>,
): CalendarioMes {
    val totalDias = diasNoMes(mes, ano)
    val porData = checkIns.associateBy { it.data }
    val prefixo = "${ano.toString().padStart(4, '0')}-${mes.toString().padStart(2, '0')}"
    val nomeMes = nomeDoMes(mes)

    val dias = (1..totalDias).map { dia ->
        val dataIso = "$prefixo-${dia.toString().padStart(2, '0')}"
        val status = checkInParaStatus(porData[dataIso])
        DiaCalendario(
            dia = dia,
            dataIso = dataIso,
            ehHoje = dataIso == hojeIso,
            status = status,
            descricaoAcessivel = descricaoAcessivelDia(dia, nomeMes, status, dataIso == hojeIso),
        )
    }

    return CalendarioMes(
        ano = ano,
        mes = mes,
        nomeMes = nomeMes,
        diaSemanaInicio = diaDaSemana(ano, mes, 1),
        dias = dias,
    )
}

fun checkInParaStatus(checkIn: CheckIn?): StatusDiaCheckIn = when {
    checkIn == null -> StatusDiaCheckIn.NENHUM
    checkIn.treinoFeito && checkIn.dietaSeguida -> StatusDiaCheckIn.AMBOS
    checkIn.treinoFeito -> StatusDiaCheckIn.TREINO
    checkIn.dietaSeguida -> StatusDiaCheckIn.DIETA
    else -> StatusDiaCheckIn.NENHUM
}

fun mesAnteriorDe(ano: Int, mes: Int): Pair<Int, Int> =
    if (mes <= 1) (ano - 1) to 12 else ano to (mes - 1)

fun proximoMesDe(ano: Int, mes: Int): Pair<Int, Int> =
    if (mes >= 12) (ano + 1) to 1 else ano to (mes + 1)

// Algoritmo de Sakamoto: retorna 0 = Domingo .. 6 = Sábado.
internal fun diaDaSemana(ano: Int, mes: Int, dia: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (mes < 3) ano - 1 else ano
    return (y + y / 4 - y / 100 + y / 400 + t[mes - 1] + dia) % 7
}

private fun nomeDoMes(mes: Int): String = when (mes) {
    1 -> "Janeiro"
    2 -> "Fevereiro"
    3 -> "Março"
    4 -> "Abril"
    5 -> "Maio"
    6 -> "Junho"
    7 -> "Julho"
    8 -> "Agosto"
    9 -> "Setembro"
    10 -> "Outubro"
    11 -> "Novembro"
    12 -> "Dezembro"
    else -> ""
}

private fun descricaoAcessivelDia(
    dia: Int,
    nomeMes: String,
    status: StatusDiaCheckIn,
    ehHoje: Boolean,
): String {
    val base = if (ehHoje) "$dia de ${nomeMes.lowercase()}, hoje" else "$dia de ${nomeMes.lowercase()}"
    return when (status) {
        StatusDiaCheckIn.AMBOS -> "$base: treino e dieta feitos"
        StatusDiaCheckIn.TREINO -> "$base: treino feito"
        StatusDiaCheckIn.DIETA -> "$base: dieta feita"
        StatusDiaCheckIn.NENHUM -> "$base: sem registro"
    }
}

