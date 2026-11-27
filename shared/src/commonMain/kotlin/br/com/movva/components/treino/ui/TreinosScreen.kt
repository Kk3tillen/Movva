package br.com.movva.components.treino.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.components.treino.domain.Treino
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.ui.BotaoPrincipal
import br.com.movva.core.util.dataDeHojeIso
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TreinosScreen(
    onTreinoClick: (Treino) -> Unit = {},
    onNovoTreinoClick: () -> Unit = {},
    viewModel: TreinoViewModel = koinViewModel(),
) {
    val state by viewModel.treinosState.collectAsStateWithLifecycle()

    // recarrega sempre que a lista volta a aparecer (ex: depois de finalizar um treino no detalhe)
    LaunchedEffect(Unit) { viewModel.carregarTreinos() }

    TreinosContent(
        state = state,
        onTreinoClick = onTreinoClick,
        onNovoTreinoClick = onNovoTreinoClick,
        onTentarNovamente = viewModel::carregarTreinos,
    )
}

@Composable
fun TreinosContent(
    state: TreinosUiState,
    modifier: Modifier = Modifier,
    onTreinoClick: (Treino) -> Unit = {},
    onNovoTreinoClick: () -> Unit = {},
    onTentarNovamente: () -> Unit = {},
    hoje: String = dataDeHojeIso(),
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        bottomBar = {
            BotaoPrincipal(
                texto = "Adicionar treino",
                onClick = onNovoTreinoClick,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.primary,
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
            ) {
                Text(
                    text = "Meus treinos",
                    color = colors.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                when (state) {
                    is TreinosUiState.Loading -> Centralizado {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics { contentDescription = "Carregando treinos" }
                        )
                    }

                    is TreinosUiState.Vazio -> Centralizado {
                        Text(
                            text = "Nenhum treino registrado ainda",
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }

                    is TreinosUiState.Erro -> Centralizado {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.mensagem, color = colors.error, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                        }
                    }

                    is TreinosUiState.Dados -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.treinos, key = { it.id }) { treino ->
                                TreinoItem(
                                    treino = treino,
                                    hoje = hoje,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onTreinoClick(treino) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Centralizado(conteudo: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        conteudo()
    }
}

@Preview
@Composable
private fun TreinosPreviewCarregando() {
    MovvaTheme {
        TreinosContent(state = TreinosUiState.Loading)
    }
}

@Preview
@Composable
private fun TreinosPreviewComItens() {
    MovvaTheme {
        TreinosContent(
            state = TreinosUiState.Dados(
                listOf(
                    Treino(
                        id = "1",
                        nome = "Treino de pernas",
                        tipo = "Musculação",
                        duracaoMin = 50,
                        data = "2026-10-09",
                        observacoes = "Aumentei a carga no agachamento",
                        ultimaExecucao = "2026-10-16",
                    ),
                    Treino(
                        id = "2",
                        nome = "Corrida leve",
                        tipo = "Corrida",
                        duracaoMin = null,
                        data = "2026-10-08",
                        observacoes = null,
                        ultimaExecucao = "2026-10-12",
                    ),
                ),
            ),
            hoje = "2026-10-16",
        )
    }
}

@Preview
@Composable
private fun TreinosPreviewVazia() {
    MovvaTheme {
        TreinosContent(state = TreinosUiState.Vazio)
    }
}

@Preview
@Composable
private fun TreinosPreviewErro() {
    MovvaTheme {
        TreinosContent(state = TreinosUiState.Erro("Não foi possível carregar os treinos"))
    }
}
