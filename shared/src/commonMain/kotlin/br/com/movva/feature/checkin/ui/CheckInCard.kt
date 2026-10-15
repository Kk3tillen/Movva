package br.com.movva.feature.checkin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
fun CheckInCard(
    state: CheckInUiState,
    onTreinoFeitoChange: (Boolean) -> Unit,
    onDietaSeguidaChange: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Text(
            text = "Check-in Diário",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Registre sua consistência de hoje",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.carregando) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            return@Column
        }

        CheckInLinha(
            texto = "Treinei hoje",
            marcado = state.treinoFeito,
            habilitado = !state.salvando,
            onMarcadoChange = onTreinoFeitoChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        CheckInLinha(
            texto = "Segui a dieta hoje",
            marcado = state.dietaSeguida,
            habilitado = !state.salvando,
            onMarcadoChange = onDietaSeguidaChange,
        )

        if (state.erro != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.erro, color = colors.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val feito = state.registrado && !state.alterado
        val texto = when {
            state.salvando -> "Salvando…"
            feito -> "Check-in feito"
            state.registrado -> "Atualizar check-in"
            else -> "Fazer Check-in"
        }

        Button(
            onClick = onRegistrar,
            enabled = !state.salvando,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (feito) colors.surface else colors.primary,
                contentColor = if (feito) colors.onSurfaceVariant else colors.onPrimary,
                disabledContainerColor = if (feito) colors.surface else colors.primary,
                disabledContentColor = if (feito) colors.onSurfaceVariant else colors.onPrimary,
            ),
        ) {
            Text(texto)
        }
    }
}

@Composable
private fun CheckInLinha(
    texto: String,
    marcado: Boolean,
    habilitado: Boolean,
    onMarcadoChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { onMarcadoChange(!marcado) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = marcado, onCheckedChange = onMarcadoChange, enabled = habilitado)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
    }
}

@Preview
@Composable
private fun CheckInCardPreviewInicial() {
    MovvaTheme {
        CheckInCard(
            state = CheckInUiState(carregando = false),
            onTreinoFeitoChange = {},
            onDietaSeguidaChange = {},
            onRegistrar = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun CheckInCardPreviewFeito() {
    MovvaTheme {
        CheckInCard(
            state = CheckInUiState(
                carregando = false,
                treinoFeito = true,
                dietaSeguida = true,
                registrado = true,
                alterado = false,
            ),
            onTreinoFeitoChange = {},
            onDietaSeguidaChange = {},
            onRegistrar = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
