package br.com.movva.feature.home

import br.com.movva.auth.AuthRepository
import br.com.movva.auth.LoggedInUser
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.ItemRefeicao
import br.com.movva.components.treino.data.TreinoRepository
import br.com.movva.components.treino.data.TreinosResultado
import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.Treino
import br.com.movva.components.treino.domain.TreinoDetalhe
import br.com.movva.feature.checkin.data.CheckInRepository
import br.com.movva.feature.checkin.data.CheckInsMesResultado
import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.feature.home.ui.CalendarioUiState
import br.com.movva.feature.home.ui.HomeUiState
import br.com.movva.feature.home.ui.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun carregandoParaSucessoComDadosDeHojeECalendario() = runTest(testDispatcher) {
        val checkInRepo = FakeCheckInRepository(
            streak = 5,
            checkInHoje = CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = false),
            porMes = mapOf(
                (2026 to 10) to CheckInsMesResultado(
                    listOf(CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = false))
                )
            ),
        )
        val treinoRepo = FakeTreinoRepository(
            TreinosResultado.Sucesso(
                listOf(
                    Treino(
                        id = "t1",
                        nome = "Pernas",
                        tipo = "Musculação",
                        duracaoMin = 45,
                        data = "2026-10-01",
                        observacoes = null,
                        ultimaExecucao = "2026-10-16",
                    )
                )
            )
        )
        val itemArroz = ItemRefeicao(
            alimentoId = "a1",
            nome = "Arroz",
            icone = "🍚",
            quantidadeG = 200.0,
            kcal = 260.0,
            proteinaG = 5.0,
            carboG = 56.0,
            gorduraG = 0.6,
        )
        val dietaRepo = FakeDietaRepository(
            DietasResultado.Sucesso(
                listOf(
                    Dieta(
                        id = "d1",
                        tipoRefeicao = "Almoço",
                        descricao = "Arroz",
                        data = "2026-10-16",
                        itens = listOf(itemArroz),
                        feitaHoje = false,
                    )
                )
            )
        )

        val vm = HomeViewModel(
            authRepository = FakeAuthRepository("Ana"),
            checkInRepository = checkInRepo,
            treinoRepository = treinoRepo,
            dietaRepository = dietaRepo,
            hojeProvider = { "2026-10-16" },
        )

        assertIs<HomeUiState.Carregando>(vm.uiState.value)
        vm.carregar()
        advanceUntilIdle()

        val state = assertIs<HomeUiState.Sucesso>(vm.uiState.value)
        assertEquals("Ana", state.nomeUsuario)
        assertEquals(5, state.streakDias)
        assertTrue(state.treinoHoje.feitoHoje)
        assertEquals(listOf("Pernas"), state.treinoHoje.nomesTreinos)
        assertEquals(260, state.dietaHoje.kcalTotal)
        assertIs<CalendarioUiState.Sucesso>(state.calendario)
    }

    @Test
    fun carregandoParaVazioQuandoNaoHaRegistros() = runTest(testDispatcher) {
        val vm = HomeViewModel(
            authRepository = FakeAuthRepository("Ana"),
            checkInRepository = FakeCheckInRepository(streak = 0, checkInHoje = null),
            treinoRepository = FakeTreinoRepository(TreinosResultado.Sucesso(emptyList())),
            dietaRepository = FakeDietaRepository(DietasResultado.Sucesso(emptyList())),
            hojeProvider = { "2026-10-16" },
        )

        vm.carregar()
        advanceUntilIdle()

        val state = assertIs<HomeUiState.Vazio>(vm.uiState.value)
        assertIs<CalendarioUiState.Vazio>(state.calendario)
    }

    @Test
    fun carregandoParaErroQuandoRepositoriosFalham() = runTest(testDispatcher) {
        val vm = HomeViewModel(
            authRepository = FakeAuthRepository("Ana"),
            checkInRepository = FakeCheckInRepository(streak = 0, checkInHoje = null),
            treinoRepository = FakeTreinoRepository(TreinosResultado.Erro("Falha de rede")),
            dietaRepository = FakeDietaRepository(DietasResultado.Erro("Falha de rede")),
            hojeProvider = { "2026-10-16" },
        )

        vm.carregar()
        advanceUntilIdle()

        val state = assertIs<HomeUiState.Erro>(vm.uiState.value)
        assertEquals("Falha de rede", state.mensagem)
    }

    @Test
    fun trocaDeMesAtualizaCalendarioETrataVazio() = runTest(testDispatcher) {
        val checkInRepo = FakeCheckInRepository(
            streak = 2,
            checkInHoje = CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = false),
            porMes = mapOf(
                (2026 to 10) to CheckInsMesResultado(
                    listOf(CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = false))
                ),
                (2026 to 9) to CheckInsMesResultado(emptyList()),
            ),
        )
        val vm = HomeViewModel(
            authRepository = FakeAuthRepository("Ana"),
            checkInRepository = checkInRepo,
            treinoRepository = FakeTreinoRepository(TreinosResultado.Sucesso(emptyList())),
            dietaRepository = FakeDietaRepository(DietasResultado.Sucesso(emptyList())),
            hojeProvider = { "2026-10-16" },
        )

        vm.carregar()
        advanceUntilIdle()
        assertIs<CalendarioUiState.Sucesso>((vm.uiState.value as HomeUiState.Sucesso).calendario)

        vm.mesAnterior()
        advanceUntilIdle()
        val calSetembro = assertIs<CalendarioUiState.Vazio>((vm.uiState.value as HomeUiState.Sucesso).calendario)
        assertEquals(9, calSetembro.calendario.mes)

        vm.proximoMes()
        advanceUntilIdle()
        val calOutubro = assertIs<CalendarioUiState.Sucesso>((vm.uiState.value as HomeUiState.Sucesso).calendario)
        assertEquals(10, calOutubro.calendario.mes)
    }

    @Test
    fun atualizaDepoisDeNovoCheckIn() = runTest(testDispatcher) {
        val checkInRepo = FakeCheckInRepository(streak = 0, checkInHoje = null)
        val treinoRepo = FakeTreinoRepository(TreinosResultado.Sucesso(emptyList()))
        val dietaRepo = FakeDietaRepository(DietasResultado.Sucesso(emptyList()))

        val vm = HomeViewModel(
            authRepository = FakeAuthRepository("Ana"),
            checkInRepository = checkInRepo,
            treinoRepository = treinoRepo,
            dietaRepository = dietaRepo,
            hojeProvider = { "2026-10-16" },
        )

        vm.carregar()
        advanceUntilIdle()
        assertIs<HomeUiState.Vazio>(vm.uiState.value)

        checkInRepo.streak = 1
        checkInRepo.checkInHoje = CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = false)
        checkInRepo.porMes = mapOf(
            (2026 to 10) to CheckInsMesResultado(listOf(checkInRepo.checkInHoje!!))
        )

        vm.carregar()
        advanceUntilIdle()

        val state = assertIs<HomeUiState.Sucesso>(vm.uiState.value)
        assertEquals(1, state.streakDias)
        assertTrue(state.treinoHoje.feitoHoje)
    }

    private class FakeAuthRepository(private val nome: String) : AuthRepository {
        override suspend fun login(email: String, senha: String): Result<Unit> = Result.success(Unit)
        override suspend fun logout() {}
        override suspend fun currentUser(): LoggedInUser = LoggedInUser("u1", nome, "ana")
    }

    private class FakeCheckInRepository(
        var streak: Int,
        var checkInHoje: CheckIn?,
        var porMes: Map<Pair<Int, Int>, CheckInsMesResultado> = emptyMap(),
    ) : CheckInRepository {
        override suspend fun getCheckInDeHoje(): CheckIn? = checkInHoje
        override suspend fun marcarTreinoFeito(data: String): Result<Unit> = Result.success(Unit)
        override suspend fun marcarDietaSeguida(data: String): Result<Unit> = Result.success(Unit)
        override suspend fun getStreakAtual(): Int = streak
        override suspend fun getCheckInsDoMes(ano: Int, mes: Int): CheckInsMesResultado =
            porMes[ano to mes] ?: CheckInsMesResultado(emptyList())
    }

    private class FakeTreinoRepository(
        private val resultado: TreinosResultado,
    ) : TreinoRepository {
        override fun getTreinos(): Flow<TreinosResultado> = flowOf(resultado)
        override suspend fun salvar(nome: String, tipo: String, duracaoMin: Int?, observacoes: String?, exercicios: List<Exercicio>): Result<Unit> = Result.success(Unit)
        override suspend fun getTreinoDetalhe(id: String): Result<TreinoDetalhe> = Result.failure(NotImplementedError())
        override suspend fun finalizarTreino(treinoId: String, exercicios: List<Exercicio>): Result<Unit> = Result.success(Unit)
    }

    private class FakeDietaRepository(
        private val resultado: DietasResultado,
    ) : DietaRepository {
        override fun getDietas(): Flow<DietasResultado> = flowOf(resultado)
        override suspend fun getDieta(id: String): Result<Dieta> = Result.failure(NotImplementedError())
        override suspend fun getRefeicaoDeHoje(tipoRefeicao: String): Dieta? = null
        override suspend fun salvarRefeicao(id: String?, tipoRefeicao: String, itens: List<ItemRefeicao>): Result<Dieta> = Result.failure(NotImplementedError())
        override suspend fun marcarFeitaHoje(dietaId: String, feita: Boolean): Result<Unit> = Result.success(Unit)
        override suspend fun getAlimentosFrequentes(limite: Int): List<String> = emptyList()
    }
}
