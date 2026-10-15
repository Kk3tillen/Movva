package br.com.movva.feature.checkin

import br.com.movva.feature.checkin.data.CheckInRepositoryImpl
import br.com.movva.feature.checkin.ui.CheckInViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    ) = CheckInViewModel(CheckInRepositoryImpl(remoto, local, auth))

    @Test
    fun estadoInicial_semCheckInSalvo_mostraFazerCheckIn() {
        val viewModel = criarViewModel()

        val estado = viewModel.uiState.value
        assertFalse(estado.carregando)
        assertFalse(estado.treinoFeito)
        assertFalse(estado.dietaSeguida)
        assertFalse(estado.registrado)
        assertFalse(estado.alterado)
    }

    @Test
    fun registrarCheckIn_marcaComoRegistradoENaoAlterado() {
        val viewModel = criarViewModel()

        viewModel.onTreinoFeitoChange(true)
        viewModel.onDietaSeguidaChange(true)
        viewModel.registrarCheckIn()

        val estado = viewModel.uiState.value
        assertTrue(estado.registrado) // UI mostra "Check-in feito"
        assertFalse(estado.alterado)
        assertFalse(estado.salvando)
        assertNull(estado.erro)
    }

    @Test
    fun aposRegistrar_alterarCheckboxMarcaComoAlterado() {
        val viewModel = criarViewModel()

        viewModel.registrarCheckIn()
        assertTrue(viewModel.uiState.value.registrado)

        viewModel.onTreinoFeitoChange(true)

        val estado = viewModel.uiState.value
        assertTrue(estado.registrado)
        assertTrue(estado.alterado) // UI mostra "Atualizar check-in"
    }

    @Test
    fun antesDoPrimeiroSalvamento_marcarCheckboxNaoMostraComoAlterado() {
        val viewModel = criarViewModel()

        viewModel.onTreinoFeitoChange(true)

        val estado = viewModel.uiState.value
        assertFalse(estado.registrado)
        assertFalse(estado.alterado) // UI continua mostrando "Fazer Check-in"
    }

    @Test
    fun registrarCheckIn_depoisDeAtualizar_voltaParaNaoAlterado() {
        val viewModel = criarViewModel()
        viewModel.registrarCheckIn()

        viewModel.onDietaSeguidaChange(true)
        assertTrue(viewModel.uiState.value.alterado)

        viewModel.registrarCheckIn()

        val estado = viewModel.uiState.value
        assertTrue(estado.registrado)
        assertFalse(estado.alterado) // UI volta a mostrar "Check-in feito"
    }

    @Test
    fun registrarCheckIn_semUsuarioLogado_mostraErroENaoFicaRegistrado() {
        val viewModel = criarViewModel(auth = FakeAuthRepository(usuario = null))

        viewModel.registrarCheckIn()

        val estado = viewModel.uiState.value
        assertFalse(estado.registrado)
        assertFalse(estado.salvando)
        assertTrue(estado.erro != null)
    }

    @Test
    fun carregar_restauraCheckInJaRegistradoAnteriormente() {
        val local = FakeCheckInLocalDataSource()
        val primeiroViewModel = criarViewModel(local = local)
        primeiroViewModel.onTreinoFeitoChange(true)
        primeiroViewModel.registrarCheckIn()

        // simula reabrir o app no mesmo dia: novo ViewModel, mesmo cache local
        val segundoViewModel = criarViewModel(local = local)

        val estado = segundoViewModel.uiState.value
        assertTrue(estado.registrado)
        assertTrue(estado.treinoFeito)
        assertFalse(estado.alterado)
    }
}
