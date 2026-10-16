package br.com.movva.components.treino.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.TreinoDetalhe
import br.com.movva.components.treino.domain.formatarData
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import br.com.movva.core.ui.BotaoTracejado
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TreinoDetalheScreen(
    treinoId: String,
    onVoltar: () -> Unit,
    viewModel: TreinoDetalheViewModel = koinViewModel {
        parametersOf(treinoId)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarSaida by remember { mutableStateOf(false) }

    val temRegistroNaoSalvo = (state as? TreinoDetalheUiState.Dados)
        ?.podeFinalizar == true
    val tentarVoltar = { if (temRegistroNaoSalvo) confirmarSaida = true else onVoltar() }

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = tentarVoltar,
    )

    TreinoDetalheContent(
        state = state,
        onVoltar = tentarVoltar,
        onTentarNovamente = viewModel::carregar,
        onExercicioClick = viewModel::onExercicioClick,
        onRepsChange = viewModel::onRepsChange,
        onCargaChange = viewModel::onCargaChange,
        onSerieCheck = viewModel::onSerieCheck,
        onAdicionarSerie = viewModel::onAdicionarSerie,
        onRemoverSerie = viewModel::onRemoverSerie,
        onReiniciar = viewModel::onReiniciar,
        onFinalizar = viewModel::finalizar,
    )

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair sem finalizar?") },
            text = { Text("As séries registradas neste treino serão perdidas.") },
            confirmButton = {
                TextButton(onClick = { confirmarSaida = false; onVoltar() }) {
                    Text("Sair", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSaida = false }) { Text("Continuar treino") }
            },
        )
    }
}

@Composable
fun TreinoDetalheContent(
    state: TreinoDetalheUiState,
    onVoltar: () -> Unit = {},
    onTentarNovamente: () -> Unit = {},
    onExercicioClick: (String) -> Unit = {},
    onRepsChange: (String, Int, String) -> Unit = { _, _, _ -> },
    onCargaChange: (String, Int, String) -> Unit = { _, _, _ -> },
    onSerieCheck: (String, Int) -> Unit = { _, _ -> },
    onAdicionarSerie: (String) -> Unit = {},
    onRemoverSerie: (String, Int) -> Unit = { _, _ -> },
    onReiniciar: () -> Unit = {},
    onFinalizar: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val dados = state as? TreinoDetalheUiState.Dados

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Cabecalho(
            registro = dados?.registro,
            onVoltar = onVoltar,
            onReiniciar = onReiniciar,
        )

        when (state) {
            is TreinoDetalheUiState.Loading -> Centralizado {
                CircularProgressIndicator()
            }

            is TreinoDetalheUiState.Erro -> Centralizado {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.mensagem, color = colors.error, fontSize = 14.sp)
                    TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                }
            }

            is TreinoDetalheUiState.Dados -> {
                if (state.registro.exercicios.isEmpty()) {
                    Centralizado {
                        Text(
                            text = "Nenhum exercício neste treino ainda",
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.registro.exercicios, key = { it.id }) { exercicio ->
                            ExercicioCard(
                                exercicio = exercicio,
                                expandido = state.registro.expandidoId == exercicio.id,
                                onClick = { onExercicioClick(exercicio.id) },
                                onRepsChange = { i, v -> onRepsChange(exercicio.id, i, v) },
                                onCargaChange = { i, v -> onCargaChange(exercicio.id, i, v) },
                                onSerieCheck = { i -> onSerieCheck(exercicio.id, i) },
                                onAdicionarSerie = { onAdicionarSerie(exercicio.id) },
                                onRemoverSerie = { i -> onRemoverSerie(exercicio.id, i) },
                            )
                        }
                    }

                    if (state.podeFinalizar) {
                        RodapeFinalizar(
                            texto = if (state.registro.treino.feitoHoje) "Atualizar treino de hoje" else "Finalizar treino",
                            finalizando = state.finalizando,
                            erro = state.erroFinalizar,
                            onFinalizar = onFinalizar,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecalho(
    registro: RegistroTreino?,
    onVoltar: () -> Unit,
    onReiniciar: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var menuAberto by remember { mutableStateOf(false) }
    val progresso by animateFloatAsState(registro?.progresso ?: 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = colors.onPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (registro != null) {
                Box {
                    IconButton(onClick = { menuAberto = true }) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "Mais opções", tint = colors.onPrimary)
                    }
                    DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                        DropdownMenuItem(
                            text = { Text("Reiniciar treino") },
                            enabled = registro.temSeriesRegistradas,
                            onClick = { menuAberto = false; onReiniciar() },
                        )
                    }
                }
            }
        }

        if (registro == null) return@Column

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = registro.treino.rotulo,
                color = colors.onPrimary.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = registro.treino.nome,
                color = colors.onPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                registro.treino.duracaoEstimadaMin?.let { minutos ->
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$minutos min", color = colors.onPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = "${registro.concluidos}/${registro.total} exercícios",
                    color = colors.onPrimary,
                    fontSize = 13.sp,
                )
            }
            // o mesmo treino pode ser feito várias vezes: "feito" só vale para hoje
            val treino = registro.treino
            val status = when {
                treino.feitoHoje -> "✓ Feito hoje"
                treino.ultimaExecucao != null -> "Última vez: ${formatarData(treino.ultimaExecucao)}"
                else -> null
            }
            status?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(colors.onPrimary, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progresso },
                color = colors.onPrimary,
                trackColor = colors.onPrimary.copy(alpha = 0.3f),
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )
        }
    }
}

@Composable
private fun ExercicioCard(
    exercicio: ExercicioUi,
    expandido: Boolean,
    onClick: () -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onCargaChange: (Int, String) -> Unit,
    onSerieCheck: (Int) -> Unit,
    onAdicionarSerie: () -> Unit,
    onRemoverSerie: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        ) {
            IndicadorCircular(concluido = exercicio.concluido, tamanho = 28.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercicio.nome,
                    color = colors.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(text = exercicio.resumo, color = colors.onSurfaceVariant, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (exercicio.concluido) {
                Selo(texto = "Concluído", cor = VerdeConcluido)
            } else {
                Selo(texto = "Registrar", cor = colors.primary)
            }
            Icon(
                imageVector = if (expandido) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expandido) "Recolher" else "Expandir",
                tint = colors.onSurfaceVariant,
            )
        }

        AnimatedVisibility(visible = expandido) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 16.dp),
            ) {
                exercicio.series.forEachIndexed { indice, serie ->
                    LinhaSerie(
                        numero = indice + 1,
                        serie = serie,
                        podeRemover = exercicio.series.size > 1,
                        onRepsChange = { onRepsChange(indice, it) },
                        onCargaChange = { onCargaChange(indice, it) },
                        onCheck = { onSerieCheck(indice) },
                        onRemover = { onRemoverSerie(indice) },
                    )
                }
                BotaoTracejado(
                    texto = "+ Adicionar série",
                    onClick = onAdicionarSerie,
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LinhaSerie(
    numero: Int,
    serie: SerieUi,
    podeRemover: Boolean,
    onRepsChange: (String) -> Unit,
    onCargaChange: (String) -> Unit,
    onCheck: () -> Unit,
    onRemover: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Série $numero",
            color = colors.onBackground,
            fontSize = 14.sp,
            modifier = Modifier.width(64.dp),
        )
        CampoNumero(
            valor = serie.reps,
            onChange = onRepsChange,
            sufixo = "reps",
            keyboardType = KeyboardType.Number,
            habilitado = !serie.concluida,
            erro = !serie.repsValidas,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        CampoNumero(
            valor = serie.carga,
            onChange = onCargaChange,
            sufixo = "kg",
            keyboardType = KeyboardType.Decimal,
            habilitado = !serie.concluida,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onCheck, enabled = serie.concluida || serie.repsValidas) {
            IndicadorCircular(
                concluido = serie.concluida,
                tamanho = 28.dp,
                contentDescription = if (serie.concluida) "Desmarcar série $numero" else "Marcar série $numero como feita",
            )
        }
        if (podeRemover) {
            IconButton(onClick = onRemover, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remover série $numero",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

@Composable
private fun CampoNumero(
    valor: String,
    onChange: (String) -> Unit,
    sufixo: String,
    keyboardType: KeyboardType,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    erro: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val corBorda = if (erro) colors.error else colors.outline

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.background)
            .border(1.dp, corBorda, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
    ) {
        BasicTextField(
            value = valor,
            onValueChange = onChange,
            enabled = habilitado,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                color = if (habilitado) colors.onBackground else colors.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            ),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = sufixo, color = colors.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun IndicadorCircular(
    concluido: Boolean,
    tamanho: Dp,
    contentDescription: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(tamanho)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .then(
                if (concluido) {
                    Modifier.background(VerdeConcluido)
                } else {
                    Modifier.border(2.dp, colors.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                }
            ),
    ) {
        if (concluido) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(tamanho * 0.6f),
            )
        }
    }
}

@Composable
internal fun Selo(texto: String, cor: Color) {
    Text(
        text = texto,
        color = cor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(cor.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun RodapeFinalizar(
    texto: String,
    finalizando: Boolean,
    erro: String?,
    onFinalizar: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        erro?.let {
            Text(text = it, color = colors.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = onFinalizar,
            enabled = !finalizando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (finalizando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    texto,
                    color = colors.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
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

private val treinoExemplo = TreinoDetalhe(
    id = "1",
    nome = "Perna e glúteo",
    rotulo = "MUSCULAÇÃO",
    duracaoEstimadaMin = 42,
    ultimaExecucao = "2026-10-12",
    exercicios = listOf(
        Exercicio("a", "Agachamento livre", 4, 10, 40.0),
        Exercicio("b", "Leg press 45°", 4, 10, 90.0),
        Exercicio("c", "Cadeira extensora", 3, 12, 30.0),
        Exercicio("d", "Elevação pélvica", 4, 10, 60.0),
        Exercicio("e", "Panturrilha em pé", 3, 15, null),
    ),
)

private fun registroExemplo(): RegistroTreino {
    var registro = RegistroTreino.iniciar(treinoExemplo)
    // concluir "a" e "b" abre automaticamente o próximo pendente ("c")
    repeat(4) { registro = registro.alternarSerie("a", it) }
    repeat(4) { registro = registro.alternarSerie("b", it) }
    return registro.alternarSerie("c", 0)
}

@Preview
@Composable
private fun TreinoDetalhePreviewEmAndamento() {
    MovvaTheme {
        TreinoDetalheContent(state = TreinoDetalheUiState.Dados(registroExemplo(), modificado = true))
    }
}

@Preview
@Composable
private fun TreinoDetalhePreviewInicio() {
    MovvaTheme {
        TreinoDetalheContent(state = TreinoDetalheUiState.Dados(RegistroTreino.iniciar(treinoExemplo)))
    }
}

@Preview
@Composable
private fun TreinoDetalhePreviewCarregando() {
    MovvaTheme {
        TreinoDetalheContent(state = TreinoDetalheUiState.Loading)
    }
}

@Preview
@Composable
private fun TreinoDetalhePreviewErro() {
    MovvaTheme {
        TreinoDetalheContent(state = TreinoDetalheUiState.Erro("Treino não encontrado"))
    }
}
