package br.com.movva.components.dieta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.components.dieta.domain.CatalogoAlimentos
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.TotaisRefeicao
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.ui.BotaoPrincipal
import br.com.movva.core.util.dataDeHojeIso
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun DietasScreen(
    onRegistrarClick: () -> Unit = {},
    onDietaClick: (Dieta) -> Unit = {},
    mensagem: String? = null,
    onMensagemMostrada: () -> Unit = {},
    viewModel: DietaViewModel = koinViewModel(),
) {
    val state by viewModel.dietasState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.carregarDietas() }

    LaunchedEffect(viewModel) {
        viewModel.mensagens.collect { texto ->
            snackbarHostState.showSnackbar(texto)
        }
    }

    val escopo = rememberCoroutineScope()
    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            escopo.launch { snackbarHostState.showSnackbar(mensagem) }
            onMensagemMostrada()
        }
    }

    DietasContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRegistrarClick = onRegistrarClick,
        onDietaClick = onDietaClick,
        onAlternarSelecao = viewModel::onAlternarSelecao,
        onFinalizarDieta = viewModel::onFinalizarDieta,
    )
}

@Composable
fun DietasContent(
    state: DietasUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRegistrarClick: () -> Unit = {},
    onDietaClick: (Dieta) -> Unit = {},
    onAlternarSelecao: (String) -> Unit = {},
    onFinalizarDieta: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val dados = state as? DietasUiState.Dados
    val temSelecionadas = (dados?.selecionadas?.isNotEmpty() == true)

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (temSelecionadas) {
                    val textoBotao = if (dados.finalizando) {
                        "Finalizando..."
                    } else if (dados.dietaSeguidaHoje) {
                        "Atualizar dieta de hoje 🔥"
                    } else {
                        "Finalizar dieta de hoje 🔥"
                    }
                    BotaoPrincipal(
                        texto = textoBotao,
                        onClick = onFinalizarDieta,
                        habilitado = !dados.finalizando,
                    )
                }
                BotaoPrincipal(
                    texto = "Registrar refeição",
                    onClick = onRegistrarClick,
                )
            }
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
                    text = "Minhas refeições",
                    color = colors.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                dados?.let { d ->
                    val totalHoje = d.dietas
                        .filter { it.id in d.selecionadas || d.dietaSeguidaHoje }
                        .fold(TotaisRefeicao()) { acc, dieta -> acc + dieta.totais }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total selecionado: ${totalHoje.kcal.roundToInt()} kcal · " +
                            "P ${totalHoje.proteinaG.roundToInt()}g · " +
                            "C ${totalHoje.carboG.roundToInt()}g · " +
                            "G ${totalHoje.gorduraG.roundToInt()}g",
                        color = colors.onPrimary,
                        fontSize = 13.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
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
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp),
                            ) {
                                items(state.dietas, key = { it.id }) { dieta ->
                                    val isConcluida = dieta.feitaHoje || (state.dietaSeguidaHoje && state.dietas.none { it.feitaHoje })
                                    val isSelecionada = dieta.id in state.selecionadas
                                    DietaItem(
                                        dieta = if (isConcluida != dieta.feitaHoje) dieta.copy(feitaHoje = isConcluida) else dieta,
                                        selecionada = isSelecionada,
                                        onAlternarSelecao = { onAlternarSelecao(dieta.id) },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onDietaClick(dieta) },
                                    )
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
    val arroz = CatalogoAlimentos.porId("arroz-branco")!!
    val frango = CatalogoAlimentos.porId("frango-grelhado")!!
    MovvaTheme {
        DietasContent(
            state = DietasUiState.Dados(
                dietas = listOf(
                    Dieta(
                        id = "1",
                        tipoRefeicao = "Almoço",
                        descricao = "Arroz branco cozido, Peito de frango grelhado",
                        data = "2026-10-16",
                        itens = listOf(arroz.item(1.0, arroz.porcoes.first()), frango.item(120.0, null)),
                    ),
                    Dieta(
                        id = "2",
                        tipoRefeicao = "Café da manhã",
                        descricao = "Pão integral com ovo e uma fruta",
                        data = "2026-10-08",
                    ),
                ),
                selecionadas = setOf("1"),
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
