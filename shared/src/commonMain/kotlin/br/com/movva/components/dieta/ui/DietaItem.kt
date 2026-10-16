package br.com.movva.components.dieta.ui

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import kotlin.math.roundToInt
import br.com.movva.components.dieta.domain.Dieta

@Composable
fun DietaItem(
    dieta: Dieta,
    hoje: String,
    onMarcarFeita: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dieta.tipoRefeicao,
                color = colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dieta.descricao,
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (dieta.itens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${dieta.totais.kcal.roundToInt()} kcal",
                    color = VerdeConcluido,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            val ultima = dieta.ultimaVezFeita
            if (!dieta.feitaHoje && ultima != null && ultima != hoje) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Última vez: ${formatarData(ultima)}",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }

        IconButton(onClick = onMarcarFeita) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (dieta.feitaHoje) {
                            Modifier.background(VerdeConcluido)
                        } else {
                            Modifier.border(2.dp, colors.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                        }
                    )
                    .semantics {
                        contentDescription = if (dieta.feitaHoje) {
                            "Desmarcar ${dieta.tipoRefeicao} como feita hoje"
                        } else {
                            "Marcar ${dieta.tipoRefeicao} como feita hoje"
                        }
                    },
            ) {
                if (dieta.feitaHoje) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
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
private fun DietaItemPreview() {
    MovvaTheme {
        DietaItem(
            dieta = Dieta(
                id = "1",
                tipoRefeicao = "Almoço",
                descricao = "Arroz, feijão, frango grelhado e salada",
                data = "2026-10-09",
                feitaHoje = true,
                ultimaVezFeita = "2026-10-16",
            ),
            hoje = "2026-10-16",
            onMarcarFeita = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun DietaItemPreviewDescricaoLonga() {
    MovvaTheme {
        DietaItem(
            dieta = Dieta(
                id = "2",
                tipoRefeicao = "Jantar",
                descricao = "Omelete com três ovos, queijo, tomate e orégano, acompanhado de uma fatia de pão integral e uma porção de salada verde com azeite e limão",
                data = "2026-10-08",
                ultimaVezFeita = "2026-10-12",
            ),
            hoje = "2026-10-16",
            onMarcarFeita = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}