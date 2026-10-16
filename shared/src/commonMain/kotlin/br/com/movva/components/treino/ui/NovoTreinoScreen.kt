package br.com.movva.components.treino.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.ui.CampoTexto
import br.com.movva.components.treino.domain.NovoTreinoForm
import br.com.movva.components.treino.domain.validarNovoTreino
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NovoTreinoScreen(
    viewModel: TreinoViewModel = koinViewModel(),
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.salvo) {
        if (state.salvo) {
            snackbarHostState.showSnackbar("Treino salvo!")
            viewModel.consumirSalvo()
        }
    }

    NovoTreinoContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNomeChange = viewModel::onNomeChange,
        onTipoChange = viewModel::onTipoChange,
        onDuracaoChange = viewModel::onDuracaoChange,
        onDataChange = viewModel::onDataChange,
        onObservacoesChange = viewModel::onObservacoesChange,
        onSalvar = viewModel::salvar,
    )
}

@Composable
fun NovoTreinoContent(
    state: NovoTreinoUiState,
    snackbarHostState: SnackbarHostState,
    onNomeChange: (String) -> Unit = {},
    onTipoChange: (String) -> Unit = {},
    onDuracaoChange: (String) -> Unit = {},
    onDataChange: (String) -> Unit = {},
    onObservacoesChange: (String) -> Unit = {},
    onSalvar: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Novo treino",
                color = colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

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
                label = "Data",
                valor = state.form.data,
                onChange = onDataChange,
                placeholder = "AAAA-MM-DD",
                erro = state.erros.data,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                label = "Observações (opcional)",
                valor = state.form.observacoes,
                onChange = onObservacoesChange,
                singleLine = false,
                minLines = 3,
            )

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
}

@Preview
@Composable
private fun NovoTreinoPreviewVazio() {
    MovvaTheme {
        NovoTreinoContent(
            state = NovoTreinoUiState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
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
                    data = "2026-10-09",
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun NovoTreinoPreviewComErros() {
    MovvaTheme {
        NovoTreinoContent(
            state = NovoTreinoUiState(erros = validarNovoTreino(NovoTreinoForm())),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}