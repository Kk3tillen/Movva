package br.com.movva.components.dieta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import br.com.movva.components.dieta.domain.Dieta
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DietasScreen(
    viewModel: DietaViewModel = koinViewModel(),
) {
    val state by viewModel.dietasState.collectAsStateWithLifecycle()
    DietasContent(state = state)
}

@Composable
fun DietasContent(
    state: DietasUiState,
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
                    text = "Minhas refeições",
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
                    is DietasUiState.Loading -> Centralizado {
                        CircularProgressIndicator()
                    }

                    is DietasUiState.Erro -> Centralizado {
                        Text(text = state.mensagem, color = colors.error, fontSize = 14.sp)
                    }

                    is DietasUiState.Dados -> {
                        if (state.dietas.isEmpty()) {
                            Centralizado {
                                Text(
                                    text = "Nenhuma refeição registrada ainda",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 14.sp,
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(state.dietas, key = { it.id }) { dieta ->
                                    DietaItem(dieta = dieta)
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
private fun DietasPreviewComItens() {
    MovvaTheme {
        DietasContent(
            state = DietasUiState.Dados(
                listOf(
                    Dieta(
                        id = "1",
                        tipoRefeicao = "Almoço",
                        descricao = "Arroz, feijão, frango grelhado e salada",
                        data = "2026-10-09",
                    ),
                    Dieta(
                        id = "2",
                        tipoRefeicao = "Café da manhã",
                        descricao = "Pão integral com ovo e uma fruta",
                        data = "2026-10-08",
                    ),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun DietasPreviewVazia() {
    MovvaTheme {
        DietasContent(state = DietasUiState.Dados(emptyList()))
    }
}