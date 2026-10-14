package br.com.movva.feature.dieta.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.ui.CampoTexto
import br.com.movva.feature.dieta.domain.NovaDietaForm
import br.com.movva.feature.dieta.domain.validarNovaDieta
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NovaDietaScreen(
    viewModel: DietaViewModel = koinViewModel(),
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.salvo) {
        if (state.salvo) {
            snackbarHostState.showSnackbar("Refeição salva!")
            viewModel.consumirSalvo()
        }
    }

    NovaDietaContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onTipoRefeicaoChange = viewModel::onTipoRefeicaoChange,
        onDescricaoChange = viewModel::onDescricaoChange,
        onDataChange = viewModel::onDataChange,
        onSalvar = viewModel::salvar,
    )
}

@Composable
fun NovaDietaContent(
    state: NovaDietaUiState,
    snackbarHostState: SnackbarHostState,
    onTipoRefeicaoChange: (String) -> Unit = {},
    onDescricaoChange: (String) -> Unit = {},
    onDataChange: (String) -> Unit = {},
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
                text = "Nova refeição",
                color = colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            CampoTexto(
                label = "Tipo de refeição",
                valor = state.form.tipoRefeicao,
                onChange = onTipoRefeicaoChange,
                placeholder = "Ex: Café da manhã, almoço, jantar, lanche",
                erro = state.erros.tipoRefeicao,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                label = "Descrição",
                valor = state.form.descricao,
                onChange = onDescricaoChange,
                placeholder = "Ex: Arroz, feijão, frango e salada",
                erro = state.erros.descricao,
                singleLine = false,
                minLines = 3,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                label = "Data",
                valor = state.form.data,
                onChange = onDataChange,
                placeholder = "AAAA-MM-DD",
                erro = state.erros.data,
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
                        "Salvar refeição",
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
private fun NovaDietaPreviewVazia() {
    MovvaTheme {
        NovaDietaContent(
            state = NovaDietaUiState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun NovaDietaPreviewPreenchida() {
    MovvaTheme {
        NovaDietaContent(
            state = NovaDietaUiState(
                form = NovaDietaForm(
                    tipoRefeicao = "Almoço",
                    descricao = "Arroz, feijão, frango grelhado e salada",
                    data = "2026-10-09",
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun NovaDietaPreviewComErros() {
    MovvaTheme {
        NovaDietaContent(
            state = NovaDietaUiState(erros = validarNovaDieta(NovaDietaForm())),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}