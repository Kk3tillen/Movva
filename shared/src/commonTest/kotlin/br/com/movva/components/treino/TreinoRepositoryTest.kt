package br.com.movva.components.treino

import br.com.movva.components.treino.data.TreinoRepositoryImpl
import br.com.movva.feature.checkin.FakeAuthRepository
import br.com.movva.feature.checkin.FakeCheckInLocalDataSource
import br.com.movva.feature.checkin.FakeCheckInRemoteDataSource
import br.com.movva.feature.checkin.data.CheckInRepositoryImpl
import br.com.movva.feature.checkin.domain.CheckIn
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TreinoRepositoryTest {

    @Test
    fun salvar_marcaAutomaticamenteOCheckInDoDiaDoTreino() = runTest {
        val treinoLocal = FakeTreinoLocalDataSource()
        val treinoRemoto = FakeTreinoRemoteDataSource()
        val checkInLocal = FakeCheckInLocalDataSource()
        val checkInRepository = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), checkInLocal, FakeAuthRepository())
        val repositorio = TreinoRepositoryImpl(treinoRemoto, treinoLocal, FakeAuthRepository(), checkInRepository)

        val resultado = repositorio.salvar(
            nome = "Treino de pernas",
            tipo = "Força",
            duracaoMin = 60,
            data = "2026-10-16",
            observacoes = null
        )

        assertTrue(resultado.isSuccess)
        val checkIn = checkInLocal.getCheckInDoDia("user-1", "2026-10-16")
        assertNotNull(checkIn)
        assertEquals(true, checkIn.treinoFeito)
    }

    @Test
    fun salvar_preservaDietaSeguidaJaRegistradaParaODia() = runTest {
        val treinoLocal = FakeTreinoLocalDataSource()
        val treinoRemoto = FakeTreinoRemoteDataSource()
        val checkInLocal = FakeCheckInLocalDataSource()
        checkInLocal.salvar(
            "user-1",
            CheckIn(data = "2026-10-16", treinoFeito = false, dietaSeguida = true),
            sincronizado = true
        )
        val checkInRepository = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), checkInLocal, FakeAuthRepository())
        val repositorio = TreinoRepositoryImpl(treinoRemoto, treinoLocal, FakeAuthRepository(), checkInRepository)

        repositorio.salvar(
            nome = "Treino de costas",
            tipo = "Força",
            duracaoMin = 45,
            data = "2026-10-16",
            observacoes = null
        )

        val checkIn = checkInLocal.getCheckInDoDia("user-1", "2026-10-16")
        assertEquals(true, checkIn?.treinoFeito)
        assertEquals(true, checkIn?.dietaSeguida)
    }

    @Test
    fun salvar_quandoCheckInFalha_aindaAssimRetornaSucessoParaOTreino() = runTest {
        val treinoLocal = FakeTreinoLocalDataSource()
        val treinoRemoto = FakeTreinoRemoteDataSource()
        val checkInRepository = CheckInRepositoryImpl(
            FakeCheckInRemoteDataSource(),
            FakeCheckInLocalDataSource(),
            FakeAuthRepository(usuario = null)
        )
        val repositorio = TreinoRepositoryImpl(treinoRemoto, treinoLocal, FakeAuthRepository(), checkInRepository)

        val resultado = repositorio.salvar(
            nome = "Treino de peito",
            tipo = "Força",
            duracaoMin = 50,
            data = "2026-10-16",
            observacoes = null
        )

        assertTrue(resultado.isSuccess)
        assertTrue(treinoLocal.getTreinos("user-1").isNotEmpty())
    }
}
