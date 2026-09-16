package br.com.movva.feature.perfil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.movva.core.clipboard.ClipboardManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val MovvaOrange = Color(0xFFE34200)

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = koinViewModel(),
    clipboardManager: ClipboardManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is PerfilUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is PerfilUiState.Erro -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { Text(state.mensagem) }

                is PerfilUiState.Dados -> PerfilConteudo(
                    nome = state.nome,
                    userTag = state.userTag,
                    onCopiar = { clipboardManager.copy(state.userTag) },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
private fun PerfilConteudo(
    nome: String,
    userTag: String,
    onCopiar: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var mostrarFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovvaOrange, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(88.dp)
                .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nome.firstOrNull()?.uppercase() ?: "?",
                color = MovvaOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
        }

        Text(
            text = nome,
            modifier = Modifier.padding(top = 12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userTag,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            IconButton(onClick = {
                onCopiar()
                mostrarFeedback = true
            }) {
                Text("⧉", color = Color.White)
            }
        }
    }

    LaunchedEffect(mostrarFeedback) {
        if (mostrarFeedback) {
            snackbarHostState.showSnackbar("Copiado!")
            mostrarFeedback = false
        }
    }
}
