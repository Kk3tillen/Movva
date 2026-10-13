package br.com.movva.feature.treino.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.feature.treino.domain.Treino

@Composable
fun TreinoItem(
    treino: Treino,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = treino.nome,
                color = colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatarData(treino.data),
                color = colors.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = listOfNotNull(
                treino.tipo,
                treino.duracaoMin?.let { "$it min" },
            ).joinToString(" · "),
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
        )

        treino.observacoes?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = colors.onBackground,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// "2026-10-09" -> "09/10/2026". Se o formato for outro, mostra como veio.
private fun formatarData(data: String): String {
    val partes = data.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else data
}

@Preview
@Composable
private fun TreinoItemPreviewCompleto() {
    MovvaTheme {
        TreinoItem(
            treino = Treino(
                id = "1",
                nome = "Treino de pernas",
                tipo = "Musculação",
                duracaoMin = 50,
                data = "2026-10-09",
                observacoes = "Aumentei a carga no agachamento",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun TreinoItemPreviewSemOpcionais() {
    MovvaTheme {
        TreinoItem(
            treino = Treino(
                id = "2",
                nome = "Corrida leve",
                tipo = "Corrida",
                duracaoMin = null,
                data = "2026-10-08",
                observacoes = null,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}