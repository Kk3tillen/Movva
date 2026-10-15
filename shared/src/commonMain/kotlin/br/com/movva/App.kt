package br.com.movva

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.movva.auth.AuthGateViewModel
import br.com.movva.auth.LoginScreen
import br.com.movva.auth.StartDestination
import br.com.movva.core.di.platformModule
import br.com.movva.core.di.providePlatformContext
import br.com.movva.core.di.rememberPlatformContext
import br.com.movva.core.network.supabaseModule
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.feature.perfil.di.perfilModule
import br.com.movva.feature.perfil.ui.PerfilScreen
import br.com.movva.modules.authModule
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.feature.treino.di.treinoModule
import br.com.movva.feature.dieta.di.dietaModule
import br.com.movva.feature.treino.ui.NovoTreinoScreen
import br.com.movva.feature.treino.ui.TreinosScreen
import br.com.movva.feature.dieta.ui.NovaDietaScreen
import br.com.movva.feature.dieta.ui.DietasScreen
import br.com.movva.feature.checkin.di.checkInModule
import br.com.movva.feature.home.ui.HomeScreen

@Composable
@Preview
fun App() {
    val platformContext = rememberPlatformContext()
    KoinApplication(application = {
        providePlatformContext(platformContext)
        modules(supabaseModule, platformModule(), perfilModule, authModule, treinoModule, dietaModule, checkInModule)
    }) {
        MovvaTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AuthGate()
            }
        }
    }
}

@Composable
fun AuthGate(
    viewModel: AuthGateViewModel = koinViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    when (destination) {
        StartDestination.Loading -> SplashScreen()
        StartDestination.Login -> LoginScreen(
            onLoginSuccess = viewModel::onLoginSuccess,
            onCreateAccountClick = { }
        )
        StartDestination.Home -> MainTabs(onLogout = viewModel::logout)
    }
}

private enum class MainTab(val titulo: String) {
    Inicio("Início"),
    Perfil("Perfil"),
}

// Navegação simples por abas: ainda não há um grafo de navegação no app,
// então isto só garante que Home (check-in) e Perfil (logout) fiquem acessíveis.
@Composable
private fun MainTabs(onLogout: () -> Unit) {
    var abaSelecionada by remember { mutableStateOf(MainTab.Inicio) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = abaSelecionada.ordinal, modifier = Modifier.fillMaxWidth()) {
            MainTab.entries.forEach { aba ->
                Tab(
                    selected = abaSelecionada == aba,
                    onClick = { abaSelecionada = aba },
                    text = { Text(aba.titulo) },
                )
            }
        }

        when (abaSelecionada) {
            MainTab.Inicio -> HomeScreen()
            MainTab.Perfil -> PerfilScreen(onLogout = onLogout)
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
