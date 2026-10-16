package br.com.movva.components.treino.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import br.com.movva.components.treino.domain.ExercicioErros
import br.com.movva.components.treino.domain.ExercicioForm
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.ui.BotaoTracejado
import br.com.movva.core.ui.CampoTexto
import br.com.movva.components.treino.domain.NovoTreinoForm
import br.com.movva.components.treino.domain.validarNovoTreino
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NovoTreinoScreen(
    onVoltar: () -> Unit,
    viewModel: TreinoViewModel = koinViewModel(),
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = onVoltar,
    )

    // depois de salvar volta para a lista, que já é recarregada pelo ViewModel
    LaunchedEffect(state.salvo) {
        if (state.salvo) {
            viewModel.consumirSalvo()
            onVoltar()
        }
    }

    NovoTreinoContent(
        state = state,
        onVoltar = onVoltar,
        onNomeChange = viewModel::onNomeChange,
        onTipoChange = viewModel::onTipoChange,
        onDuracaoChange = viewModel::onDuracaoChange,
        onObservacoesChange = viewModel::onObservacoesChange,
        onAdicionarExercicio = viewModel::onAdicionarExercicio,
        onRemoverExercicio = viewModel::onRemoverExercicio,
        onExercicioChange = viewModel::onExercicioChange,
        onSalvar = viewModel::salvar,
    )
}

@Composable
fun NovoTreinoContent(
    state: NovoTreinoUiState,
    onVoltar: () -> Unit = {},
    onNomeChange: (String) -> Unit = {},
    onTipoChange: (String) -> Unit = {},
    onDuracaoChange: (String) -> Unit = {},
    onObservacoesChange: (String) -> Unit = {},
    onAdicionarExercicio: () -> Unit = {},
    onRemoverExercicio: (String) -> Unit = {},
    onExercicioChange: (ExercicioForm) -> Unit = {},
    onSalvar: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar, modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.onBackground,
                )
            }
            Text(
                text = "Novo treino",
                color = colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoTexto(
            label = "Nome do treino",
            valor = state.form.nome,
            onChange = onNomeChange,
            placeholder = "Ex: Treino de pernas",
            erro = state.erros.nome,
        )
        Spacer(modifier = Modifier.height(16.dp))

        CampoTexto(
            label = "Tipo",
            valor = state.form.tipo,
            onChange = onTipoChange,
            placeholder = "Ex: Musculação, corrida",
            erro = state.erros.tipo,
        )
        Spacer(modifier = Modifier.height(16.dp))

        CampoTexto(
            label = "Duração em minutos (opcional)",
            valor = state.form.duracao,
            onChange = onDuracaoChange,
            placeholder = "Ex: 50",
            erro = state.erros.duracao,
            keyboardType = KeyboardType.Number,
        )
        Spacer(modifier = Modifier.height(16.dp))

        CampoTexto(
            label = "Observações (opcional)",
            valor = state.form.observacoes,
            onChange = onObservacoesChange,
            singleLine = false,
            minLines = 3,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Exercícios",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))

        state.form.exercicios.forEachIndexed { indice, exercicio ->
            ExercicioFormCard(
                exercicio = exercicio,
                erros = state.erros.exercicios.getOrNull(indice) ?: ExercicioErros(),
                onChange = onExercicioChange,
                onRemover = { onRemoverExercicio(exercicio.id) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        BotaoTracejado(texto = "+ Adicionar exercício", onClick = onAdicionarExercicio)

        state.erroGeral?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = colors.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSalvar,
            enabled = !state.salvando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (state.salvando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    "Salvar treino",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun ExercicioFormCard(
    exercicio: ExercicioForm,
    erros: ExercicioErros,
    onChange: (ExercicioForm) -> Unit,
    onRemover: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            CampoTexto(
                label = "Exercício",
                valor = exercicio.nome,
                onChange = { onChange(exercicio.copy(nome = it)) },
                placeholder = "Ex: Agachamento livre",
                erro = erros.nome,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
            )
            IconButton(onClick = onRemover) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remover exercício",
                    tint = colors.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.padding(end = 8.dp)) {
            CampoTexto(
                label = "Séries",
                valor = exercicio.series,
                onChange = { v -> onChange(exercicio.copy(series = v.filter { it.isDigit() }.take(2))) },
                erro = erros.series,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            CampoTexto(
                label = "Repetições",
                valor = exercicio.reps,
                onChange = { v -> onChange(exercicio.copy(reps = v.filter { it.isDigit() }.take(3))) },
                erro = erros.reps,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            CampoTexto(
                label = "Carga (kg)",
                valor = exercicio.carga,
                onChange = { v -> onChange(exercicio.copy(carga = v.take(6))) },
                placeholder = "Opcional",
                erro = erros.carga,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun NovoTreinoPreviewVazio() {
    MovvaTheme {
        NovoTreinoContent(state = NovoTreinoUiState())
    }
}

@Preview
@Composable
private fun NovoTreinoPreviewPreenchido() {
    MovvaTheme {
        NovoTreinoContent(
            state = NovoTreinoUiState(
                form = NovoTreinoForm(
                    nome = "Treino de pernas",
                    tipo = "Musculação",
                    duracao = "50",
                    exercicios = listOf(
                        ExercicioForm(id = "a", nome = "Agachamento livre", series = "4", reps = "10", carga = "40"),
                        ExercicioForm(id = "b", nome = "Leg press 45°", series = "4", reps = "12", carga = "90"),
                    ),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun NovoTreinoPreviewComErros() {
    MovvaTheme {
        val form = NovoTreinoForm(exercicios = listOf(ExercicioForm(id = "a", reps = "")))
        NovoTreinoContent(
            state = NovoTreinoUiState(form = form, erros = validarNovoTreino(form)),
        )
    }
}
