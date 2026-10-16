package br.com.movva.components.dieta

import br.com.movva.components.dieta.data.DietaRepositoryImpl
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

class DietaRepositoryTest {

    @Test
    fun salvar_marcaAutomaticamenteOCheckInDoDiaDaRefeicao() = runTest {
        val dietaLocal = FakeDietaLocalDataSource()
        val dietaRemoto = FakeDietaRemoteDataSource()
        val checkInLocal = FakeCheckInLocalDataSource()
        val checkInRepository = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), checkInLocal, FakeAuthRepository())
        val repositorio = DietaRepositoryImpl(dietaRemoto, dietaLocal, FakeAuthRepository(), checkInRepository)

        val resultado = repositorio.salvar(
            tipoRefeicao = "Almoço",
            descricao = "Frango com arroz",
            data = "2026-10-16"
        )

        assertTrue(resultado.isSuccess)
        val checkIn = checkInLocal.getCheckInDoDia("user-1", "2026-10-16")
        assertNotNull(checkIn)
        assertEquals(true, checkIn.dietaSeguida)
    }

    @Test
    fun salvar_preservaTreinoFeitoJaRegistradoParaODia() = runTest {
        val dietaLocal = FakeDietaLocalDataSource()
        val dietaRemoto = FakeDietaRemoteDataSource()
        val checkInLocal = FakeCheckInLocalDataSource()
        checkInLocal.salvar(
            "user-1",
            CheckIn(data = "2026-10-16", treinoFeito = true, dietaSeguida = false),
            sincronizado = true
        )
        val checkInRepository = CheckInRepositoryImpl(FakeCheckInRemoteDataSource(), checkInLocal, FakeAuthRepository())
        val repositorio = DietaRepositoryImpl(dietaRemoto, dietaLocal, FakeAuthRepository(), checkInRepository)

        repositorio.salvar(
            tipoRefeicao = "Jantar",
            descricao = "Salada",
            data = "2026-10-16"
        )

        val checkIn = checkInLocal.getCheckInDoDia("user-1", "2026-10-16")
        assertEquals(true, checkIn?.treinoFeito)
        assertEquals(true, checkIn?.dietaSeguida)
    }
}
