package br.com.movva.feature.checkin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.movva.core.theme.MovvaTheme

@Composable
fun StreakHeader(
    state: CheckInUiState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.primary, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        if (state.carregando) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.onPrimary,
                )
            }
            return@Column
        }

        val saudacao = if (state.nomeUsuario.isNotBlank()) "Olá, ${state.nomeUsuario}" else "Olá"
        Text(
            text = saudacao,
            color = colors.onPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Vamos manter o ritmo hoje?",
            color = colors.onPrimary,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(colors.onPrimary, RoundedCornerShape(50)),
        ) {
            Text(
                text = textoStreak(state.streakDias),
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun textoStreak(dias: Int): String = when (dias) {
    0 -> "🔥 Comece sua streak hoje"
    1 -> "🔥 1 dia seguido"
    else -> "🔥 $dias dias seguidos"
}

@Preview
@Composable
private fun StreakHeaderPreviewCarregando() {
    MovvaTheme {
        StreakHeader(
            state = CheckInUiState(carregando = true),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun StreakHeaderPreviewComStreak() {
    MovvaTheme {
        StreakHeader(
            state = CheckInUiState(carregando = false, nomeUsuario = "Ana", streakDias = 12),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun StreakHeaderPreviewSemStreak() {
    MovvaTheme {
        StreakHeader(
            state = CheckInUiState(carregando = false, nomeUsuario = "Ana", streakDias = 0),
            modifier = Modifier.padding(16.dp),
        )
    }
}
