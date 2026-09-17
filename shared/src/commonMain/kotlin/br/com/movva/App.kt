package br.com.movva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.movva.auth.LoginScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        var isLoggedIn by remember { mutableStateOf(false) }

        if (isLoggedIn) {
            Column(
                modifier = Modifier.safeContentPadding().fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Login realizado com sucesso!")
                Button(onClick = { isLoggedIn = false }) {
                    Text("Sair")
                }
            }
        } else {
            LoginScreen(
                modifier = Modifier.safeContentPadding().fillMaxSize(),
                onLoginSuccess = { isLoggedIn = true },
                onCreateAccountClick = { },
            )
        }
    }
}