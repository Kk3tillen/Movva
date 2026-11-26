package br.com.movva.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.movva.core.theme.VerdeConcluido

@Composable
fun CardTreinoHoje(
    resumo: TreinoHojeResumo,
    onIrParaTreino: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitulo = when {
        resumo.feitoHoje && resumo.nomesTreinos.isNotEmpty() -> resumo.nomesTreinos.joinToString(", ")
        resumo.feitoHoje -> "Treino registrado"
        else -> "Nenhum treino hoje"
    }

    val descricao = if (resumo.feitoHoje) {
        "Treino de hoje: $subtitulo. Feito hoje."
    } else {
        "Treino de hoje: nenhum treino hoje. Toque para abrir a aba Treinos."
    }

    CardAtividadeHoje(
        titulo = "Treino de hoje",
        subtitulo = subtitulo,
        icone = Icons.Filled.FitnessCenter,
        concluido = resumo.feitoHoje,
        textoSelo = if (resumo.feitoHoje) "✓ Feito hoje" else "Ir para Treino",
        descricaoAcessivel = descricao,
        onClick = onIrParaTreino,
        modifier = modifier,
    )
}

@Composable
fun CardDietaHoje(
    resumo: DietaHojeResumo,
    onIrParaDieta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitulo = when {
        resumo.totalRefeicoes == 0 && !resumo.dietaSeguidaHoje -> "Dieta pendente hoje"
        resumo.dietaSeguidaHoje && resumo.refeicoesFeitas == 0 && resumo.totalRefeicoes > 0 -> {
            val labelRefeicao = if (resumo.totalRefeicoes == 1) "refeição" else "refeições"
            "${resumo.kcalTotal} kcal · ${resumo.totalRefeicoes} $labelRefeicao"
        }
        resumo.kcalTotal > 0 ->
            "${resumo.kcalFeitas} / ${resumo.kcalTotal} kcal · ${resumo.refeicoesFeitas}/${resumo.totalRefeicoes} refeições"
        else ->
            "${resumo.kcalFeitas} kcal · ${resumo.refeicoesFeitas} refeições feitas"
    }

    val descricao = if (resumo.dietaSeguidaHoje) {
        "Dieta de hoje: $subtitulo. Concluída."
    } else {
        "Dieta de hoje: $subtitulo. Toque para abrir a aba Dieta."
    }

    CardAtividadeHoje(
        titulo = "Dieta de hoje",
        subtitulo = subtitulo,
        icone = Icons.Filled.Restaurant,
        concluido = resumo.dietaSeguidaHoje,
        textoSelo = if (resumo.dietaSeguidaHoje) "✓ Feito hoje" else "Ir para Dieta",
        descricaoAcessivel = descricao,
        onClick = onIrParaDieta,
        modifier = modifier,
    )
}

@Composable
private fun CardAtividadeHoje(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    concluido: Boolean,
    textoSelo: String,
    descricaoAcessivel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val corIcone = if (concluido) VerdeConcluido else colors.primary
    val corSelo = if (concluido) VerdeConcluido else colors.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = descricaoAcessivel
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(corIcone.copy(alpha = 0.14f)),
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = corIcone,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = colors.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        SeloAtividade(texto = textoSelo, cor = corSelo)
    }
}

@Composable
private fun SeloAtividade(texto: String, cor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(RoundedCornerShape(50))
            .background(cor.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = texto,
            color = cor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
