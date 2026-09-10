package br.com.movva

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.movva.auth.AuthGateViewModel
import br.com.movva.auth.StartDestination
import br.com.movva.modules.appModules
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
@Preview
fun App() {
    KoinApplication(application = { modules(appModules) }) {
        MaterialTheme {
            AuthGate(
                onLogin = { },
                onHome = { }
            )
        }
    }
}

@Composable
fun AuthGate(
    viewModel: AuthGateViewModel = koinViewModel(),
    onLogin: () -> Unit,
    onHome: () -> Unit
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    when (destination) {
        StartDestination.Loading -> SplashScreen()
        StartDestination.Login -> onLogin()
        StartDestination.Home -> onHome()
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