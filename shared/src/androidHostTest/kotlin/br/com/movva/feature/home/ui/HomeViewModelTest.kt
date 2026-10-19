package br.com.movva.feature.home.ui

import br.com.movva.auth.AuthRepository
import br.com.movva.auth.LoggedInUser
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.ItemRefeicao
import br.com.movva.feature.checkin.data.CheckInRepository
import br.com.movva.feature.checkin.domain.CheckIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class CheckInRepositoryFake(
    private val checkIn: CheckIn?,
    private val streak: Int = 0,
) : CheckInRepository {
    override suspend fun getCheckInDeHoje(): CheckIn? = checkIn
    override suspend fun marcarTreinoFeito(data: String): Result<Unit> = Result.success(Unit)
    override suspend fun marcarDietaSeguida(data: String): Result<Unit> = Result.success(Unit)
    override suspend fun getStreakAtual(): Int = streak
}

private class DietaRepositoryFake(private val dietas: List<Dieta>) : DietaRepository {
    override fun getDietas(): Flow<DietasResultado> = flowOf(DietasResultado.Sucesso(dietas))
    override suspend fun getDieta(id: String): Result<Dieta> = Result.failure(NotImplementedError())
    override suspend fun getRefeicaoDeHoje(tipoRefeicao: String): Dieta? = null
    override suspend fun salvarRefeicao(
        id: String?,
        tipoRefeicao: String,
        itens: List<ItemRefeicao>,
    ): Result<Dieta> = Result.failure(NotImplementedError())
    override suspend fun marcarFeitaHoje(dietaId: String, feita: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun getAlimentosFrequentes(limite: Int): List<String> = emptyList()
}

private class AuthRepositoryFake(private val usuario: LoggedInUser?) : AuthRepository {
    override suspend fun login(email: String, senha: String): Result<Unit> = Result.success(Unit)
    override suspend fun logout() = Unit
    override suspend fun currentUser(): LoggedInUser? = usuario
}

private fun itemComKcal(kcal: Double) = ItemRefeicao(
    alimentoId = "a",
    nome = "Item",
    icone = "🍽",
    quantidadeG = 100.0,
    kcal = kcal,
    proteinaG = 0.0,
    carboG = 0.0,
    gorduraG = 0.0,
)

private val usuarioAna = LoggedInUser(id = "1", nome = "Ana", userTag = "ana#1")

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun carregaNomeDoUsuarioEStreak() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            checkInRepository = CheckInRepositoryFake(checkIn = null, streak = 7),
            dietaRepository = DietaRepositoryFake(emptyList()),
            authRepository = AuthRepositoryFake(usuarioAna),
        )
        viewModel.carregar()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.nomeUsuario)
        assertEquals(7, state.streakDias)
        assertFalse(state.carregando)
    }

    @Test
    fun refleteTreinoEDietaFeitosHojeAPartirDoCheckIn() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            checkInRepository = CheckInRepositoryFake(
                checkIn = CheckIn(data = "2026-10-19", treinoFeito = true, dietaSeguida = false),
            ),
            dietaRepository = DietaRepositoryFake(emptyList()),
            authRepository = AuthRepositoryFake(usuarioAna),
        )
        viewModel.carregar()

        val state = viewModel.uiState.value
        assertTrue(state.treinoFeitoHoje)
        assertFalse(state.dietaSeguidaHoje)
    }

    @Test
    fun semCheckInHojeTreinoEDietaFicamPendentes() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            checkInRepository = CheckInRepositoryFake(checkIn = null),
            dietaRepository = DietaRepositoryFake(emptyList()),
            authRepository = AuthRepositoryFake(usuarioAna),
        )
        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.treinoFeitoHoje)
        assertFalse(state.dietaSeguidaHoje)
    }

    @Test
    fun somaKcalSoDasDietasFeitasHoje() = runTest(dispatcher) {
        val feita = Dieta(
            id = "1", tipoRefeicao = "Almoço", descricao = "x", data = "2026-10-19",
            itens = listOf(itemComKcal(500.0)), feitaHoje = true,
        )
        val naoFeita = Dieta(
            id = "2", tipoRefeicao = "Jantar", descricao = "y", data = "2026-10-19",
            itens = listOf(itemComKcal(800.0)), feitaHoje = false,
        )

        val viewModel = HomeViewModel(
            checkInRepository = CheckInRepositoryFake(checkIn = null),
            dietaRepository = DietaRepositoryFake(listOf(feita, naoFeita)),
            authRepository = AuthRepositoryFake(usuarioAna),
        )
        viewModel.carregar()

        assertEquals(500.0, viewModel.uiState.value.kcalHoje)
    }
}
