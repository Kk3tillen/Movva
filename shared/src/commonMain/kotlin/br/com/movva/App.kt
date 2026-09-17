package br.com.movva

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
@Preview
fun App() {
    val platformContext = rememberPlatformContext()
    KoinApplication(application = {
        providePlatformContext(platformContext)
        modules(supabaseModule, platformModule(), perfilModule, authModule)
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
        StartDestination.Home -> PerfilScreen(onLogout = viewModel::logout)
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
