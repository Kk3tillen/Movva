package br.com.movva

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import br.com.movva.auth.AuthGateViewModel
import br.com.movva.auth.LoginViewModel
import br.com.movva.auth.domain.AuthRepository
import br.com.movva.components.dieta.di.dietaModule
import br.com.movva.components.dieta.domain.DietaRepository
import br.com.movva.components.dieta.ui.DietaViewModel
import br.com.movva.components.dieta.ui.RegistrarRefeicaoArgs
import br.com.movva.components.dieta.ui.RegistrarRefeicaoViewModel
import br.com.movva.components.perfil.di.perfilModule
import br.com.movva.components.perfil.domain.PerfilRepository
import br.com.movva.components.perfil.ui.PerfilViewModel
import br.com.movva.components.treino.di.treinoModule
import br.com.movva.components.treino.domain.TreinoRepository
import br.com.movva.components.treino.ui.TreinoDetalheViewModel
import br.com.movva.components.treino.ui.TreinoViewModel
import br.com.movva.core.network.supabaseModule
import br.com.movva.feature.checkin.di.checkInModule
import br.com.movva.feature.checkin.domain.CheckInRepository
import br.com.movva.feature.checkin.ui.CheckInViewModel
import br.com.movva.feature.home.di.homeModule
import br.com.movva.feature.home.ui.HomeViewModel
import br.com.movva.modules.authModule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

private class NoOpSqlCursor : SqlCursor {
    override fun next(): QueryResult<Boolean> = QueryResult.Value(false)
    override fun getString(index: Int): String? = null
    override fun getLong(index: Int): Long? = null
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null
}

private class NoOpSqlDriver : SqlDriver {
    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit
    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) = Unit
    override fun notifyListeners(vararg queryKeys: String) = Unit
    override fun currentTransaction(): Transacter.Transaction? = null

    override fun newTransaction(): QueryResult<Transacter.Transaction> =
        QueryResult.Value(
            object : Transacter.Transaction() {
                override val enclosingTransaction: Transacter.Transaction? = null
                override fun endTransaction(successful: Boolean): QueryResult<Unit> = QueryResult.Unit
            }
        )

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = QueryResult.Value(0L)

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> = mapper(NoOpSqlCursor())

    override fun close() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class KoinModulesTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun montaTodosOsModulosEResolveRepositoriosEViewModels() {
        val koinApp = startKoin {
            allowOverride(true)
            modules(
                supabaseModule,
                perfilModule,
                authModule,
                treinoModule,
                dietaModule,
                checkInModule,
                homeModule,
                // Substitui apenas dependências de plataforma (Android Context) em testes JVM.
                module {
                    single<SqlDriver> { NoOpSqlDriver() }
                    single<SupabaseClient> {
                        createSupabaseClient(
                            supabaseUrl = "https://example.supabase.co",
                            supabaseKey = "anon-key",
                        ) {
                            install(Postgrest)
                        }
                    }
                },
            )
        }
        val koin = koinApp.koin

        assertNotNull(koin.get<AuthRepository>())
        assertNotNull(koin.get<PerfilRepository>())
        assertNotNull(koin.get<TreinoRepository>())
        assertNotNull(koin.get<DietaRepository>())
        assertNotNull(koin.get<CheckInRepository>())

        assertNotNull(koin.get<AuthGateViewModel>())
        assertNotNull(koin.get<LoginViewModel>())
        assertNotNull(koin.get<PerfilViewModel>())
        assertNotNull(koin.get<TreinoViewModel>())
        assertNotNull(koin.get<TreinoDetalheViewModel> { parametersOf("treino-1") })
        assertNotNull(koin.get<DietaViewModel>())
        assertNotNull(koin.get<RegistrarRefeicaoViewModel> { parametersOf(RegistrarRefeicaoArgs()) })
        assertNotNull(koin.get<CheckInViewModel>())
        assertNotNull(koin.get<HomeViewModel>())
    }
}
