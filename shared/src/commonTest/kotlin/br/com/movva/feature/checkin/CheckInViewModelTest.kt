package br.com.movva.feature.checkin

import br.com.movva.auth.LoggedInUser
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.data.CheckInRepositoryImpl
import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.feature.checkin.ui.CheckInViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun criarViewModel(
        local: FakeCheckInLocalDataSource = FakeCheckInLocalDataSource(),
        remoto: FakeCheckInRemoteDataSource = FakeCheckInRemoteDataSource(),
        auth: FakeAuthRepository = FakeAuthRepository(),
    ) = CheckInViewModel(CheckInRepositoryImpl(remoto, local, auth), auth)

    @Test
    fun estadoInicial_semHistorico_mostraStreakZero() {
        val viewModel = criarViewModel()

        val estado = viewModel.uiState.value
        assertEquals(false, estado.carregando)
        assertEquals("Teste", estado.nomeUsuario)
        assertEquals(0, estado.streakDias)
    }

    @Test
    fun carregar_comAtividadeHoje_mostraStreakDeUmDia() = kotlinx.coroutines.test.runTest {
        val local = FakeCheckInLocalDataSource()
        local.salvar(
            "user-1",
            CheckIn(data = dataDeHojeIso(), treinoFeito = true, dietaSeguida = false),
            sincronizado = true,
        )
        val viewModel = criarViewModel(local = local)

        assertEquals(1, viewModel.uiState.value.streakDias)
    }

    @Test
    fun carregar_semUsuarioLogado_mostraStreakZeroSemErro() {
        val viewModel = criarViewModel(auth = FakeAuthRepository(usuario = null))

        val estado = viewModel.uiState.value
        assertEquals(0, estado.streakDias)
        assertEquals("", estado.nomeUsuario)
    }

    @Test
    fun carregar_usaONomeDoUsuarioLogado() {
        val auth = FakeAuthRepository(usuario = LoggedInUser(id = "user-1", nome = "Bia", userTag = "@bia"))
        val viewModel = criarViewModel(auth = auth)

        assertEquals("Bia", viewModel.uiState.value.nomeUsuario)
    }
}
