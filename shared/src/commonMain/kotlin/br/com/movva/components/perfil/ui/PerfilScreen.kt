package br.com.movva.components.perfil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.clipboard.ClipboardManager
import br.com.movva.core.theme.MovvaTheme
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PerfilScreen(
    onLogout: () -> Unit = { },
    viewModel: PerfilViewModel = koinViewModel(),
    clipboardManager: ClipboardManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    PerfilContent(
        uiState = uiState,
        onTentarNovamente = viewModel::carregarPerfil,
        onCopiar = clipboardManager::copy,
        onLogout = onLogout,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun PerfilContent(
    uiState: PerfilUiState,
    onTentarNovamente: () -> Unit = {},
    onCopiar: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is PerfilUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { contentDescription = "Carregando perfil" }
                    )
                }

                is PerfilUiState.Vazio -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nenhum dado de perfil encontrado",
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                    }
                }

                is PerfilUiState.Erro -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.mensagem, color = colors.error, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                    }
                }

                is PerfilUiState.Dados -> PerfilConteudo(
                    nome = state.nome,
                    userTag = state.userTag,
                    avatarUrl = state.avatarUrl,
                    onCopiar = { onCopiar(state.userTag) },
                    onLogout = onLogout,
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
    avatarUrl: String,
    onCopiar: () -> Unit,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var mostrarFeedback by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Perfil",
                modifier = Modifier.align(Alignment.Center),
                color = colors.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            IconButton(
                onClick = onLogout,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("⎋", color = colors.onPrimary, fontSize = 18.sp)
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(88.dp)
                .border(3.dp, colors.onPrimary.copy(alpha = 0.6f), CircleShape)
                .background(colors.onPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar de $nome",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                loading = { AvatarInicial(nome) },
                error = { AvatarInicial(nome) }
            )
        }

        Text(
            text = nome,
            modifier = Modifier.padding(top = 12.dp),
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userTag,
                color = colors.onPrimary.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            IconButton(
                onClick = {
                    onCopiar()
                    mostrarFeedback = true
                },
                modifier = Modifier.semantics { contentDescription = "Copiar identificador de usuário" }
            ) {
                Text("⧉", color = colors.onPrimary)
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

@Composable
private fun AvatarInicial(nome: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = nome.firstOrNull()?.uppercase() ?: "?",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
    }
}

@Preview
@Composable
private fun PerfilPreviewCarregando() {
    MovvaTheme {
        PerfilContent(uiState = PerfilUiState.Loading)
    }
}

@Preview
@Composable
private fun PerfilPreviewVazio() {
    MovvaTheme {
        PerfilContent(uiState = PerfilUiState.Vazio)
    }
}

@Preview
@Composable
private fun PerfilPreviewDados() {
    MovvaTheme {
        PerfilContent(
            uiState = PerfilUiState.Dados(
                nome = "Ana Silva",
                userTag = "#MOV_ANA_123",
                avatarUrl = "",
            )
        )
    }
}

@Preview
@Composable
private fun PerfilPreviewErro() {
    MovvaTheme {
        PerfilContent(uiState = PerfilUiState.Erro("Não foi possível carregar o perfil"))
    }
}
