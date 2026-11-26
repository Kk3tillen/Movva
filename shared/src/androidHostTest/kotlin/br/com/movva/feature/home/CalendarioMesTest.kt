package br.com.movva.feature.home

import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.feature.home.domain.StatusDiaCheckIn
import br.com.movva.feature.home.domain.checkInParaStatus
import br.com.movva.feature.home.domain.diaDaSemana
import br.com.movva.feature.home.domain.mesAnteriorDe
import br.com.movva.feature.home.domain.montarCalendarioMes
import br.com.movva.feature.home.domain.proximoMesDe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalendarioMesTest {

    @Test
    fun mesesCom28_29_30_31Dias() {
        assertEquals(28, montarCalendarioMes(2026, 2, "2026-02-10", emptyList()).dias.size)
        assertEquals(29, montarCalendarioMes(2024, 2, "2024-02-10", emptyList()).dias.size)
        assertEquals(30, montarCalendarioMes(2026, 4, "2026-04-10", emptyList()).dias.size)
        assertEquals(31, montarCalendarioMes(2026, 10, "2026-10-10", emptyList()).dias.size)
    }

    @Test
    fun primeiroDiaEmCadaDiaDaSemana() {
        assertEquals(0, diaDaSemana(2026, 2, 1))  // Domingo
        assertEquals(1, diaDaSemana(2026, 6, 1))  // Segunda
        assertEquals(2, diaDaSemana(2026, 9, 1))  // Terça
        assertEquals(3, diaDaSemana(2026, 4, 1))  // Quarta
        assertEquals(4, diaDaSemana(2026, 10, 1)) // Quinta
        assertEquals(5, diaDaSemana(2026, 5, 1))  // Sexta
        assertEquals(6, diaDaSemana(2026, 8, 1))  // Sábado
    }

    @Test
    fun viradaDeAno() {
        assertEquals(2025 to 12, mesAnteriorDe(2026, 1))
        assertEquals(2027 to 1, proximoMesDe(2026, 12))
        assertEquals(2026 to 9, mesAnteriorDe(2026, 10))
        assertEquals(2026 to 11, proximoMesDe(2026, 10))
    }

    @Test
    fun mapeamentoDeCheckInsParaStatus() {
        assertEquals(StatusDiaCheckIn.NENHUM, checkInParaStatus(null))
        assertEquals(StatusDiaCheckIn.NENHUM, checkInParaStatus(CheckIn("2026-10-01", treinoFeito = false, dietaSeguida = false)))
        assertEquals(StatusDiaCheckIn.TREINO, checkInParaStatus(CheckIn("2026-10-02", treinoFeito = true, dietaSeguida = false)))
        assertEquals(StatusDiaCheckIn.DIETA, checkInParaStatus(CheckIn("2026-10-03", treinoFeito = false, dietaSeguida = true)))
        assertEquals(StatusDiaCheckIn.AMBOS, checkInParaStatus(CheckIn("2026-10-04", treinoFeito = true, dietaSeguida = true)))
    }

    @Test
    fun calendarioMarcaHojeEDescricaoAcessivel() {
        val cal = montarCalendarioMes(
            ano = 2026,
            mes = 10,
            hojeIso = "2026-10-12",
            checkIns = listOf(
                CheckIn("2026-10-12", treinoFeito = true, dietaSeguida = true),
            ),
        )
        val dia12 = cal.dias[11]
        val dia13 = cal.dias[12]

        assertTrue(cal.temRegistros)
        assertTrue(dia12.ehHoje)
        assertEquals(StatusDiaCheckIn.AMBOS, dia12.status)
        assertEquals("12 de outubro, hoje: treino e dieta feitos", dia12.descricaoAcessivel)

        assertFalse(dia13.ehHoje)
        assertEquals(StatusDiaCheckIn.NENHUM, dia13.status)
    }
}

