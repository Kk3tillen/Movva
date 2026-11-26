package br.com.movva.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.feature.checkin.ui.CheckInUiState
import br.com.movva.feature.checkin.ui.StreakHeader
import br.com.movva.feature.home.domain.montarCalendarioMes
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onIrParaTreino: () -> Unit = {},
    onIrParaDieta: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onIrParaTreino = onIrParaTreino,
        onIrParaDieta = onIrParaDieta,
        onMesAnterior = viewModel::mesAnterior,
        onProximoMes = viewModel::proximoMes,
        onTentarNovamente = viewModel::carregar,
        onRecarregarCalendario = viewModel::recarregarCalendario,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onIrParaTreino: () -> Unit = {},
    onIrParaDieta: () -> Unit = {},
    onMesAnterior: () -> Unit = {},
    onProximoMes: () -> Unit = {},
    onTentarNovamente: () -> Unit = {},
    onRecarregarCalendario: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val ehTelaLarga = maxWidth >= 600.dp

            when (state) {
                is HomeUiState.Carregando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is HomeUiState.Erro -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = state.mensagem,
                            color = colors.error,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onTentarNovamente,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }

                is HomeUiState.Vazio -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            StreakHeader(
                                state = CheckInUiState(
                                    carregando = false,
                                    nomeUsuario = state.nomeUsuario,
                                    streakDias = state.streakDias,
                                ),
                            )
                        }
                        state.avisoCache?.let { aviso ->
                            item { AvisoHomeBanner(aviso) }
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surfaceVariant, RoundedCornerShape(16.dp))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Você ainda não tem registros hoje",
                                    color = colors.onBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Registre um treino ou refeição para iniciar sua sequência!",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                BotoesAtalhoHome(
                                    onIrParaTreino = onIrParaTreino,
                                    onIrParaDieta = onIrParaDieta,
                                )
                            }
                        }
                        item {
                            CalendarioFrequenciaCard(
                                state = state.calendario,
                                onMesAnterior = onMesAnterior,
                                onProximoMes = onProximoMes,
                                onTentarNovamente = onRecarregarCalendario,
                            )
                        }
                    }
                }

                is HomeUiState.Sucesso -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            StreakHeader(
                                state = CheckInUiState(
                                    carregando = false,
                                    nomeUsuario = state.nomeUsuario,
                                    streakDias = state.streakDias,
                                ),
                            )
                        }
                        state.avisoCache?.let { aviso ->
                            item { AvisoHomeBanner(aviso) }
                        }
                        item {
                            Text(
                                text = "Hoje",
                                color = colors.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item {
                            if (ehTelaLarga) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    CardTreinoHoje(
                                        resumo = state.treinoHoje,
                                        onIrParaTreino = onIrParaTreino,
                                        modifier = Modifier.weight(1f),
                                    )
                                    CardDietaHoje(
                                        resumo = state.dietaHoje,
                                        onIrParaDieta = onIrParaDieta,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CardTreinoHoje(
                                        resumo = state.treinoHoje,
                                        onIrParaTreino = onIrParaTreino,
                                    )
                                    CardDietaHoje(
                                        resumo = state.dietaHoje,
                                        onIrParaDieta = onIrParaDieta,
                                    )
                                }
                            }
                        }
                        item {
                            CalendarioFrequenciaCard(
                                state = state.calendario,
                                onMesAnterior = onMesAnterior,
                                onProximoMes = onProximoMes,
                                onTentarNovamente = onRecarregarCalendario,
                            )
                        }
                        item {
                            BotoesAtalhoHome(
                                onIrParaTreino = onIrParaTreino,
                                onIrParaDieta = onIrParaDieta,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotoesAtalhoHome(
    onIrParaTreino: () -> Unit,
    onIrParaDieta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .clip(shape)
                .background(colors.primary)
                .clickable(onClick = onIrParaTreino)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text(
                text = "+ Registrar treino",
                color = colors.onPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .clip(shape)
                .background(VerdeConcluido.copy(alpha = 0.22f))
                .clickable(onClick = onIrParaDieta)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text(
                text = "+ Registrar refeição",
                color = VerdeConcluido,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AvisoHomeBanner(aviso: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = aviso,
        color = colors.onSurfaceVariant,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Preview
@Composable
private fun HomeSucessoPreview() {
    val calendario = montarCalendarioMes(
        ano = 2026,
        mes = 10,
        hojeIso = "2026-10-16",
        checkIns = listOf(
            CheckIn("2026-10-14", treinoFeito = true, dietaSeguida = true),
            CheckIn("2026-10-15", treinoFeito = true, dietaSeguida = false),
            CheckIn("2026-10-16", treinoFeito = false, dietaSeguida = true),
        ),
    )
    MovvaTheme {
        HomeContent(
            state = HomeUiState.Sucesso(
                nomeUsuario = "Ana",
                streakDias = 12,
                treinoHoje = TreinoHojeResumo(feitoHoje = false),
                dietaHoje = DietaHojeResumo(
                    dietaSeguidaHoje = true,
                    refeicoesFeitas = 3,
                    totalRefeicoes = 4,
                    kcalFeitas = 1450,
                    kcalTotal = 2100,
                ),
                calendario = CalendarioUiState.Sucesso(calendario),
            ),
        )
    }
}

@Preview(widthDp = 840, heightDp = 900)
@Composable
private fun HomeTabletPreview() {
    val calendario = montarCalendarioMes(
        ano = 2026,
        mes = 10,
        hojeIso = "2026-10-16",
        checkIns = listOf(
            CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = true),
        ),
    )
    MovvaTheme {
        HomeContent(
            state = HomeUiState.Sucesso(
                nomeUsuario = "Ana",
                streakDias = 12,
                treinoHoje = TreinoHojeResumo(
                    feitoHoje = true,
                    nomesTreinos = listOf("Perna e glúteos"),
                ),
                dietaHoje = DietaHojeResumo(
                    dietaSeguidaHoje = true,
                    refeicoesFeitas = 4,
                    totalRefeicoes = 4,
                    kcalFeitas = 2100,
                    kcalTotal = 2100,
                ),
                calendario = CalendarioUiState.Sucesso(calendario),
            ),
        )
    }
}

@Preview
@Composable
private fun HomeVazioPreview() {
    val calendario = montarCalendarioMes(
        ano = 2026,
        mes = 10,
        hojeIso = "2026-10-16",
        checkIns = emptyList(),
    )
    MovvaTheme {
        HomeContent(
            state = HomeUiState.Vazio(
                nomeUsuario = "Ana",
                streakDias = 0,
                calendario = CalendarioUiState.Vazio(calendario),
            ),
        )
    }
}

@Preview
@Composable
private fun HomeCarregandoPreview() {
    MovvaTheme {
        HomeContent(state = HomeUiState.Carregando)
    }
}

@Preview
@Composable
private fun HomeErroPreview() {
    MovvaTheme {
        HomeContent(state = HomeUiState.Erro("Não foi possível carregar os dados."))
    }
}
