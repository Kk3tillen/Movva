package br.com.movva.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import br.com.movva.feature.checkin.domain.CheckIn
import br.com.movva.feature.home.domain.CalendarioMes
import br.com.movva.feature.home.domain.DiaCalendario
import br.com.movva.feature.home.domain.StatusDiaCheckIn
import br.com.movva.feature.home.domain.montarCalendarioMes

private val DIAS_SEMANA = listOf("D", "S", "T", "Q", "Q", "S", "S")

sealed interface CalendarioUiState {
    data class Carregando(val ano: Int, val mes: Int) : CalendarioUiState
    data class Vazio(
        val calendario: CalendarioMes,
        val avisoCache: String? = null,
    ) : CalendarioUiState
    data class Sucesso(
        val calendario: CalendarioMes,
        val avisoCache: String? = null,
    ) : CalendarioUiState
    data class Erro(
        val ano: Int,
        val mes: Int,
        val mensagem: String,
    ) : CalendarioUiState
}

@Composable
fun CalendarioFrequenciaCard(
    state: CalendarioUiState,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit,
    onDiaClick: (DiaCalendario) -> Unit = {},
    onTentarNovamente: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        when (state) {
            is CalendarioUiState.Carregando -> {
                CabecalhoCalendario(
                    titulo = "Frequência",
                    onMesAnterior = onMesAnterior,
                    onProximoMes = onProximoMes,
                    navegacaoHabilitada = false,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is CalendarioUiState.Erro -> {
                CabecalhoCalendario(
                    titulo = "Frequência",
                    onMesAnterior = onMesAnterior,
                    onProximoMes = onProximoMes,
                    navegacaoHabilitada = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.mensagem,
                        color = colors.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onTentarNovamente,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }

            is CalendarioUiState.Vazio -> {
                CabecalhoCalendario(
                    titulo = "${state.calendario.nomeMes} ${state.calendario.ano}",
                    onMesAnterior = onMesAnterior,
                    onProximoMes = onProximoMes,
                    navegacaoHabilitada = true,
                )
                state.avisoCache?.let { aviso ->
                    AvisoCacheBanner(aviso)
                }
                Spacer(modifier = Modifier.height(12.dp))
                GradeCalendario(
                    calendario = state.calendario,
                    onDiaClick = onDiaClick,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nenhum check-in registrado neste mês.",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is CalendarioUiState.Sucesso -> {
                CabecalhoCalendario(
                    titulo = "${state.calendario.nomeMes} ${state.calendario.ano}",
                    onMesAnterior = onMesAnterior,
                    onProximoMes = onProximoMes,
                    navegacaoHabilitada = true,
                )
                state.avisoCache?.let { aviso ->
                    AvisoCacheBanner(aviso)
                }
                Spacer(modifier = Modifier.height(12.dp))
                GradeCalendario(
                    calendario = state.calendario,
                    onDiaClick = onDiaClick,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LegendaCalendario()
            }
        }
    }
}

@Composable
private fun CabecalhoCalendario(
    titulo: String,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit,
    navegacaoHabilitada: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onMesAnterior,
            enabled = navegacaoHabilitada,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Mês anterior",
                tint = colors.onBackground,
            )
        }

        Text(
            text = titulo,
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        IconButton(
            onClick = onProximoMes,
            enabled = navegacaoHabilitada,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Próximo mês",
                tint = colors.onBackground,
            )
        }
    }
}

@Composable
private fun AvisoCacheBanner(aviso: String) {
    val colors = MaterialTheme.colorScheme
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = aviso,
        color = colors.onSurfaceVariant,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun GradeCalendario(
    calendario: CalendarioMes,
    onDiaClick: (DiaCalendario) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(modifier = Modifier.fillMaxWidth()) {
        DIAS_SEMANA.forEach { letra ->
            Text(
                text = letra,
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    val celulas = buildList<DiaCalendario?> {
        repeat(calendario.diaSemanaInicio) { add(null) }
        addAll(calendario.dias)
        val resto = size % 7
        if (resto != 0) repeat(7 - resto) { add(null) }
    }

    celulas.chunked(7).forEach { semana ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            semana.forEach { dia ->
                if (dia == null) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    )
                } else {
                    CelulaDia(
                        dia = dia,
                        onClick = { onDiaClick(dia) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CelulaDia(
    dia: DiaCalendario,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val fundo = when (dia.status) {
        StatusDiaCheckIn.NENHUM -> Color.Transparent
        StatusDiaCheckIn.TREINO -> colors.primary.copy(alpha = 0.16f)
        StatusDiaCheckIn.DIETA -> VerdeConcluido.copy(alpha = 0.16f)
        StatusDiaCheckIn.AMBOS -> VerdeConcluido.copy(alpha = 0.24f)
    }

    val shape = RoundedCornerShape(10.dp)
    val bordaModifier = if (dia.ehHoje) {
        Modifier.border(1.5.dp, colors.primary, shape)
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(fundo, shape)
            .then(bordaModifier)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = dia.descricaoAcessivel
            }
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = dia.dia.toString(),
            color = if (dia.ehHoje) colors.primary else colors.onBackground,
            fontSize = 13.sp,
            fontWeight = if (dia.ehHoje || dia.status != StatusDiaCheckIn.NENHUM) FontWeight.Bold else FontWeight.Normal,
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(7.dp),
        ) {
            if (dia.status == StatusDiaCheckIn.TREINO || dia.status == StatusDiaCheckIn.AMBOS) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
            if (dia.status == StatusDiaCheckIn.DIETA || dia.status == StatusDiaCheckIn.AMBOS) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(VerdeConcluido),
                )
            }
        }
    }
}

@Composable
private fun LegendaCalendario() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ItemLegenda(
            cor = colors.primary,
            ehCirculo = true,
            texto = "Treino",
        )
        ItemLegenda(
            cor = VerdeConcluido,
            ehCirculo = false,
            texto = "Dieta",
        )
    }
}

@Composable
private fun ItemLegenda(
    cor: Color,
    ehCirculo: Boolean,
    texto: String,
) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(if (ehCirculo) CircleShape else RoundedCornerShape(2.dp))
                .background(cor),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

@Preview
@Composable
private fun CalendarioSucessoPreview() {
    val calendario = montarCalendarioMes(
        ano = 2026,
        mes = 10,
        hojeIso = "2026-10-16",
        checkIns = listOf(
            CheckIn("2026-10-05", treinoFeito = true, dietaSeguida = false),
            CheckIn("2026-10-06", treinoFeito = false, dietaSeguida = true),
            CheckIn("2026-10-12", treinoFeito = true, dietaSeguida = true),
            CheckIn("2026-10-16", treinoFeito = true, dietaSeguida = true),
        ),
    )
    MovvaTheme {
        CalendarioFrequenciaCard(
            state = CalendarioUiState.Sucesso(calendario),
            onMesAnterior = {},
            onProximoMes = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun CalendarioVazioPreview() {
    val calendario = montarCalendarioMes(
        ano = 2026,
        mes = 10,
        hojeIso = "2026-10-16",
        checkIns = emptyList(),
    )
    MovvaTheme {
        CalendarioFrequenciaCard(
            state = CalendarioUiState.Vazio(calendario),
            onMesAnterior = {},
            onProximoMes = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun CalendarioCarregandoPreview() {
    MovvaTheme {
        CalendarioFrequenciaCard(
            state = CalendarioUiState.Carregando(2026, 10),
            onMesAnterior = {},
            onProximoMes = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun CalendarioErroPreview() {
    MovvaTheme {
        CalendarioFrequenciaCard(
            state = CalendarioUiState.Erro(2026, 10, "Não foi possível carregar o calendário."),
            onMesAnterior = {},
            onProximoMes = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
