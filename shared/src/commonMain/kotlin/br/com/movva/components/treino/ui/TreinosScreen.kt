package br.com.movva.components.treino.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.components.treino.domain.Treino
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun TreinosScreen(
    viewModel: TreinoViewModel = koinViewModel(),
) {
    val state by viewModel.treinosState.collectAsStateWithLifecycle()
    TreinosContent(state = state)
}

@Composable
fun TreinosContent(
    state: TreinosUiState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
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
                        CircularProgressIndicator()
                    }

                    is TreinosUiState.Erro -> Centralizado {
                        Text(text = state.mensagem, color = colors.error, fontSize = 14.sp)
                    }

                    is TreinosUiState.Dados -> {
                        if (state.treinos.isEmpty()) {
                            Centralizado {
                                Text(
                                    text = "Nenhum treino registrado ainda",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 14.sp,
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(state.treinos, key = { it.id }) { treino ->
                                    TreinoItem(treino = treino)
                                }
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
                    ),
                    Treino(
                        id = "2",
                        nome = "Corrida leve",
                        tipo = "Corrida",
                        duracaoMin = null,
                        data = "2026-10-08",
                        observacoes = null,
                    ),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun TreinosPreviewVazia() {
    MovvaTheme {
        TreinosContent(state = TreinosUiState.Dados(emptyList()))
    }
}