package br.com.movva.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.RiceBowl
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import br.com.movva.core.ui.BotaoPrincipal
import br.com.movva.core.ui.Selo
import br.com.movva.feature.checkin.ui.CheckInUiState
import br.com.movva.feature.checkin.ui.StreakHeader
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onIrParaTreinos: () -> Unit = {},
    onIrParaDieta: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // recarrega sempre que a aba Início volta a aparecer (ex: depois de finalizar um treino)
    LaunchedEffect(Unit) { viewModel.carregar() }

    HomeContent(
        state = state,
        onIrParaTreinos = onIrParaTreinos,
        onIrParaDieta = onIrParaDieta,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onIrParaTreinos: () -> Unit = {},
    onIrParaDieta: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            StreakHeader(
                state = CheckInUiState(
                    carregando = state.carregando,
                    nomeUsuario = state.nomeUsuario,
                    streakDias = state.streakDias,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hoje",
                color = colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            CardResumoDia(
                icone = Icons.Outlined.FitnessCenter,
                titulo = "Treino",
                feito = state.treinoFeitoHoje,
                subtitulo = if (state.treinoFeitoHoje) "Feito hoje" else "Ainda não treinou hoje",
                onClick = onIrParaTreinos,
            )
            Spacer(modifier = Modifier.height(12.dp))

            CardResumoDia(
                icone = Icons.Outlined.RiceBowl,
                titulo = "Dieta",
                feito = state.dietaSeguidaHoje,
                subtitulo = if (state.kcalHoje > 0) {
                    "${state.kcalHoje.roundToInt()} kcal hoje"
                } else {
                    "Nenhuma refeição registrada hoje"
                },
                onClick = onIrParaDieta,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ações rápidas",
                color = colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotaoPrincipal(texto = "Adicionar treino", onClick = onIrParaTreinos)
            Spacer(modifier = Modifier.height(12.dp))
            BotaoPrincipal(texto = "Registrar refeição", onClick = onIrParaDieta)
        }
    }
}

@Composable
private fun CardResumoDia(
    icone: ImageVector,
    titulo: String,
    feito: Boolean,
    subtitulo: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = if (feito) VerdeConcluido else colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = subtitulo, color = colors.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (feito) {
            Selo(texto = "✓ Feito", cor = VerdeConcluido)
        } else {
            Selo(texto = "Pendente", cor = colors.primary)
        }
    }
}

@Preview
@Composable
private fun HomePreviewCarregando() {
    MovvaTheme {
        HomeContent(state = HomeUiState(carregando = true))
    }
}

@Preview
@Composable
private fun HomePreviewDiaZerado() {
    MovvaTheme {
        HomeContent(
            state = HomeUiState(
                carregando = false,
                nomeUsuario = "Ana",
                streakDias = 0,
                treinoFeitoHoje = false,
                dietaSeguidaHoje = false,
                kcalHoje = 0.0,
            ),
        )
    }
}

@Preview
@Composable
private fun HomePreviewDiaCompleto() {
    MovvaTheme {
        HomeContent(
            state = HomeUiState(
                carregando = false,
                nomeUsuario = "Ana",
                streakDias = 12,
                treinoFeitoHoje = true,
                dietaSeguidaHoje = true,
                kcalHoje = 1450.0,
            ),
        )
    }
}

@Preview
@Composable
private fun HomePreviewSoTreino() {
    MovvaTheme {
        HomeContent(
            state = HomeUiState(
                carregando = false,
                nomeUsuario = "Ana",
                streakDias = 5,
                treinoFeitoHoje = true,
                dietaSeguidaHoje = false,
                kcalHoje = 0.0,
            ),
        )
    }
}
