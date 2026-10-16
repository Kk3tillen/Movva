package br.com.movva.feature.checkin

import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.core.util.diaAnteriorIso
import br.com.movva.feature.checkin.data.CheckInRepositoryImpl
import br.com.movva.feature.checkin.domain.CheckIn
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckInRepositoryTest {

    @Test
    fun marcarTreinoFeito_salvaLocalESincronizaRemoto() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        val resultado = repositorio.marcarTreinoFeito(dataDeHojeIso())

        assertTrue(resultado.isSuccess)
        assertEquals(1, remoto.enviados.size)
        assertEquals(true, remoto.enviados.first().treinoFeito)

        val salvoLocal = local.getCheckInDoDia("user-1", dataDeHojeIso())
        assertNotNull(salvoLocal)
        assertEquals(true, salvoLocal.treinoFeito)
        assertEquals(false, salvoLocal.dietaSeguida)
        assertTrue(local.getNaoSincronizados("user-1").isEmpty())
    }

    @Test
    fun marcarTreinoFeito_semUsuarioLogado_retornaFalhaENaoSalvaNada() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository(usuario = null))

        val resultado = repositorio.marcarTreinoFeito(dataDeHojeIso())

        assertTrue(resultado.isFailure)
        assertTrue(remoto.enviados.isEmpty())
    }

    @Test
    fun marcarTreinoFeito_quandoRemotoFalha_aindaAssimSalvaLocalComoSucesso() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource(falharAoEnviar = true)
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        // falha de rede não pode impedir o registro local (offline-first)
        val resultado = repositorio.marcarTreinoFeito(dataDeHojeIso())

        assertTrue(resultado.isSuccess)
        assertNotNull(local.getCheckInDoDia("user-1", dataDeHojeIso()))
        assertEquals(1, local.getNaoSincronizados("user-1").size)
    }

    @Test
    fun getCheckInDeHoje_retornaNuloQuandoNadaFoiRegistrado() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        assertNull(repositorio.getCheckInDeHoje())
    }

    @Test
    fun getCheckInDeHoje_retornaNuloSemUsuarioLogado() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository(usuario = null))

        assertNull(repositorio.getCheckInDeHoje())
    }

    @Test
    fun getCheckInDeHoje_priorizaDadoRemotoEAtualizaCacheLocal() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val hoje = dataDeHojeIso()
        remoto.semear("user-1", CheckIn(data = hoje, treinoFeito = true, dietaSeguida = true))
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        val checkIn = repositorio.getCheckInDeHoje()

        assertEquals(true, checkIn?.treinoFeito)
        assertEquals(true, checkIn?.dietaSeguida)
        assertEquals(checkIn, local.getCheckInDoDia("user-1", hoje))
    }

    @Test
    fun getCheckInDeHoje_caiParaOCacheLocalQuandoLeituraRemotaFalha() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource(falharAoLer = true)
        val hoje = dataDeHojeIso()
        // já sincronizado: não há pendente a enviar, só a leitura remota falha
        local.salvar("user-1", CheckIn(data = hoje, treinoFeito = true, dietaSeguida = false), sincronizado = true)
        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        val checkIn = repositorio.getCheckInDeHoje()

        assertEquals(true, checkIn?.treinoFeito)
        assertEquals(false, checkIn?.dietaSeguida)
    }

    @Test
    fun getStreakAtual_semHistorico_retornaZero() = runTest {
        val repositorio = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), FakeCheckInLocalDataSource(), FakeAuthRepository())

        assertEquals(0, repositorio.getStreakAtual())
    }

    @Test
    fun getStreakAtual_semUsuarioLogado_retornaZero() = runTest {
        val repositorio = CheckInRepositoryImpl(
            FakeCheckInRemoteDataSource(),
            FakeCheckInLocalDataSource(),
            FakeAuthRepository(usuario = null)
        )

        assertEquals(0, repositorio.getStreakAtual())
    }

    @Test
    fun getStreakAtual_contaDiasConsecutivosComAtividadeAteHoje() = runTest {
        val local = FakeCheckInLocalDataSource()
        val hoje = dataDeHojeIso()
        val ontem = diaAnteriorIso(hoje)
        val anteontem = diaAnteriorIso(ontem)

        local.salvar("user-1", CheckIn(data = hoje, treinoFeito = true, dietaSeguida = false), sincronizado = true)
        local.salvar("user-1", CheckIn(data = ontem, treinoFeito = false, dietaSeguida = true), sincronizado = true)
        local.salvar("user-1", CheckIn(data = anteontem, treinoFeito = true, dietaSeguida = true), sincronizado = true)

        val repositorio = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), local, FakeAuthRepository())

        assertEquals(3, repositorio.getStreakAtual())
    }

    @Test
    fun getStreakAtual_semAtividadeHoje_continuaContandoAPartirDeOntem() = runTest {
        val local = FakeCheckInLocalDataSource()
        val hoje = dataDeHojeIso()
        val ontem = diaAnteriorIso(hoje)

        // hoje ainda não fez nada, mas ontem fez: a streak não deve quebrar antes do dia acabar
        local.salvar("user-1", CheckIn(data = ontem, treinoFeito = true, dietaSeguida = false), sincronizado = true)

        val repositorio = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), local, FakeAuthRepository())

        assertEquals(1, repositorio.getStreakAtual())
    }

    @Test
    fun getStreakAtual_paraNoPrimeiroDiaSemAtividade() = runTest {
        val local = FakeCheckInLocalDataSource()
        val hoje = dataDeHojeIso()
        val ontem = diaAnteriorIso(hoje)
        val anteontem = diaAnteriorIso(ontem)

        local.salvar("user-1", CheckIn(data = hoje, treinoFeito = true, dietaSeguida = false), sincronizado = true)
        // ontem não teve nenhuma atividade: streak quebra aqui, mesmo que anteontem tenha tido
        local.salvar("user-1", CheckIn(data = ontem, treinoFeito = false, dietaSeguida = false), sincronizado = true)
        local.salvar("user-1", CheckIn(data = anteontem, treinoFeito = true, dietaSeguida = true), sincronizado = true)

        val repositorio = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), local, FakeAuthRepository())

        assertEquals(1, repositorio.getStreakAtual())
    }

    @Test
    fun getStreakAtual_sincronizaHistoricoRemotoAntesDeContar() = runTest {
        val local = FakeCheckInLocalDataSource()
        val remoto = FakeCheckInRemoteDataSource()
        val hoje = dataDeHojeIso()
        val ontem = diaAnteriorIso(hoje)

        // dados só existem no remoto (ex.: usuário logou em outro aparelho)
        remoto.semear("user-1", CheckIn(data = hoje, treinoFeito = true, dietaSeguida = false))
        remoto.semear("user-1", CheckIn(data = ontem, treinoFeito = true, dietaSeguida = false))

        val repositorio = CheckInRepositoryImpl(remoto, local, FakeAuthRepository())

        assertEquals(2, repositorio.getStreakAtual())
    }
}
