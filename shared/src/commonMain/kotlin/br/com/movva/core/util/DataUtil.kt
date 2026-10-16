package br.com.movva.core.util

// Data local do dispositivo no formato AAAA-MM-DD (mesmo formato usado em Treino/Dieta).
expect fun dataDeHojeIso(): String

// Dia anterior a uma data no formato AAAA-MM-DD, sem depender de APIs de calendário da plataforma.
fun diaAnteriorIso(dataIso: String): String {
    val partes = dataIso.split("-")
    val ano0 = partes[0].toInt()
    val mes0 = partes[1].toInt()
    val dia0 = partes[2].toInt()

    var ano = ano0
    var mes = mes0
    var dia = dia0 - 1

    if (dia < 1) {
        mes -= 1
        if (mes < 1) {
            mes = 12
            ano -= 1
        }
        dia = diasNoMes(mes, ano)
    }

    return "${ano.toString().padStart(4, '0')}-${mes.toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}"
}

private fun diasNoMes(mes: Int, ano: Int): Int = when (mes) {
    2 -> if (ehBissexto(ano)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

private fun ehBissexto(ano: Int) = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0

expect fun horaAtual(): Int
