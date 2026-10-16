package br.com.movva.components.dieta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import br.com.movva.components.dieta.domain.Alimento
import br.com.movva.components.dieta.domain.CatalogoAlimentos
import br.com.movva.components.dieta.domain.ItemRefeicao
import br.com.movva.components.dieta.domain.Porcao
import br.com.movva.components.dieta.domain.TipoRefeicao
import br.com.movva.components.dieta.domain.TotaisRefeicao
import br.com.movva.components.dieta.domain.formatarNumero
import br.com.movva.core.theme.MovvaTheme
import br.com.movva.core.theme.VerdeConcluido
import br.com.movva.core.ui.BotaoPrincipal
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun RegistrarRefeicaoScreen(
    args: RegistrarRefeicaoArgs,
    onVoltar: () -> Unit,
    onSalvo: (mensagem: String) -> Unit,
    viewModel: RegistrarRefeicaoViewModel = koinViewModel {
        parametersOf(args)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarSaida by remember { mutableStateOf(false) }
    val tentarVoltar = { if (state.temAlteracaoNaoSalva) confirmarSaida = true else onVoltar() }

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = tentarVoltar,
    )

    LaunchedEffect(state.mensagemSalvo) {
        state.mensagemSalvo?.let(onSalvo)
    }

    RegistrarRefeicaoContent(
        state = state,
        onVoltar = tentarVoltar,
        onTentarNovamente = { args.dietaId?.let(viewModel::carregarRefeicao) },
        onTipoChange = viewModel::onTipoChange,
        onBuscaChange = viewModel::onBuscaChange,
        onAlimentoClick = viewModel::onAlimentoClick,
        onItemClick = viewModel::onItemClick,
        onRemoverItem = viewModel::onRemoverItem,
        onSalvar = viewModel::salvar,
    )

    state.edicao?.let { edicao ->
        QuantidadeSheet(
            edicao = edicao,
            onPorcaoChange = viewModel::onPorcaoChange,
            onQuantidadeChange = viewModel::onQuantidadeChange,
            onConfirmar = viewModel::onConfirmarQuantidade,
            onCancelar = viewModel::onCancelarQuantidade,
        )
    }

    state.conflito?.let {
        AlertDialog(
            onDismissRequest = viewModel::onFecharConflito,
            title = { Text("Já existe um ${state.tipo.titulo} hoje") },
            text = { Text("Quer somar estes alimentos ao ${state.tipo.titulo} já registrado ou criar outra refeição?") },
            confirmButton = { TextButton(onClick = viewModel::onSomarAExistente) { Text("Somar") } },
            dismissButton = { TextButton(onClick = viewModel::onCriarOutra) { Text("Criar outra") } },
        )
    }

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair sem salvar?") },
            text = { Text("Os alimentos adicionados serão perdidos.") },
            confirmButton = {
                TextButton(onClick = { confirmarSaida = false; onVoltar() }) {
                    Text("Sair", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSaida = false }) { Text("Continuar") }
            },
        )
    }
}

@Composable
fun RegistrarRefeicaoContent(
    state: RegistrarRefeicaoUiState,
    onVoltar: () -> Unit = {},
    onTentarNovamente: () -> Unit = {},
    onTipoChange: (TipoRefeicao) -> Unit = {},
    onBuscaChange: (String) -> Unit = {},
    onAlimentoClick: (Alimento) -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    onRemoverItem: (Int) -> Unit = {},
    onSalvar: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    // com o teclado aberto o rodapé some, para não cobrir a busca
    val tecladoAberto = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = colors.onBackground)
            }
            Text(
                text = if (state.modoEdicao) "Editar refeição" else "Registrar refeição",
                color = colors.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        when {
            state.carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.erroCarregar != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.erroCarregar, color = colors.error, fontSize = 14.sp)
                    TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Rotulo("Refeição") }
                    item { ChipsTipo(selecionado = state.tipo, onTipoChange = onTipoChange) }

                    item { Spacer(Modifier.height(8.dp)); Rotulo("Adicionar alimento") }
                    item { CampoBusca(valor = state.busca, onChange = onBuscaChange) }

                    if (state.busca.isNotBlank()) {
                        if (state.resultados.isEmpty()) {
                            item {
                                Text(
                                    "Nenhum alimento encontrado",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                        items(state.resultados, key = { "r-${it.id}" }) { alimento ->
                            ResultadoAlimento(alimento = alimento, onClick = { onAlimentoClick(alimento) })
                        }
                    } else if (state.sugestoes.isNotEmpty()) {
                        item {
                            Text("Recentes e frequentes", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        }
                        items(state.sugestoes, key = { "s-${it.id}" }) { alimento ->
                            ResultadoAlimento(alimento = alimento, onClick = { onAlimentoClick(alimento) })
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)); Rotulo("Adicionados (${state.itens.size})") }

                    if (state.itens.isEmpty()) {
                        item { ListaVazia() }
                    } else {
                        itemsIndexed(state.itens) { indice, item ->
                            CardItem(
                                item = item,
                                onClick = { onItemClick(indice) },
                                onRemover = { onRemoverItem(indice) },
                            )
                        }
                    }
                }

                if (!tecladoAberto) {
                    Rodape(state = state, onSalvar = onSalvar)
                }
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ChipsTipo(selecionado: TipoRefeicao, onTipoChange: (TipoRefeicao) -> Unit) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TipoRefeicao.entries.forEach { tipo ->
            val ativo = tipo == selecionado
            Text(
                text = tipo.titulo,
                color = if (ativo) colors.onPrimary else colors.onBackground,
                fontSize = 14.sp,
                fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (ativo) colors.primary else colors.primary.copy(alpha = 0.12f))
                    .clickable { onTipoChange(tipo) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun CampoBusca(valor: String, onChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text("Ex: arroz, frango, banana...", color = colors.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpar busca", tint = colors.onSurfaceVariant)
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedTextColor = colors.onBackground,
            unfocusedTextColor = colors.onBackground,
            cursorColor = colors.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun IconeAlimento(icone: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .background(VerdeConcluido.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
    ) {
        Text(icone, fontSize = 22.sp)
    }
}

@Composable
private fun ResultadoAlimento(alimento: Alimento, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // porção padrão: a primeira medida caseira ou 100 g
    val porcao = alimento.porcoes.firstOrNull()
    val item = alimento.item(if (porcao == null) 100.0 else 1.0, porcao)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        IconeAlimento(alimento.icone)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(alimento.nome, color = colors.onBackground, fontSize = 15.sp)
            Text(item.porcaoDescricao, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
        Text("${item.kcal.roundToInt()} kcal", color = colors.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun CardItem(item: ItemRefeicao, onClick: () -> Unit, onRemover: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
    ) {
        IconeAlimento(item.icone)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.nome, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(item.porcaoDescricao, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
        Text(
            "${item.kcal.roundToInt()} kcal",
            color = VerdeConcluido,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onRemover) {
            Icon(Icons.Filled.Close, contentDescription = "Remover ${item.nome}", tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ListaVazia() {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .background(colors.primary.copy(alpha = 0.12f), CircleShape),
        ) {
            Text("🍽️", fontSize = 36.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Busque um alimento para começar", color = colors.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
private fun Rodape(state: RegistrarRefeicaoUiState, onSalvar: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        ResumoRefeicao(totais = state.totais)

        state.erro?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = colors.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(12.dp))
        BotaoPrincipal(
            texto = "Salvar refeição",
            onClick = onSalvar,
            habilitado = state.itens.isNotEmpty(),
            carregando = state.salvando,
        )
    }
}

@Composable
fun ResumoRefeicao(totais: TotaisRefeicao, titulo: String = "Total da refeição") {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdeConcluido.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, color = colors.onBackground, fontSize = 13.sp)
            Text(
                "${totais.kcal.roundToInt()} kcal",
                color = VerdeConcluido,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Macro("Proteína", totais.proteinaG)
            Macro("Carbo", totais.carboG)
            Macro("Gordura", totais.gorduraG)
        }
    }
}

@Composable
private fun Macro(nome: String, gramas: Double) {
    Text(
        "$nome ${gramas.roundToInt()}g",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 12.sp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantidadeSheet(
    edicao: EdicaoQuantidade,
    onPorcaoChange: (Porcao?) -> Unit,
    onQuantidadeChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val alimento = edicao.alimento
    val item = edicao.item

    ModalBottomSheet(
        onDismissRequest = onCancelar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
    ) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconeAlimento(alimento.icone)
                Spacer(Modifier.width(12.dp))
                Text(alimento.nome, color = colors.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Rotulo("Medida")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChipMedida("gramas", ativo = edicao.porcao == null) { onPorcaoChange(null) }
                alimento.porcoes.forEach { porcao ->
                    ChipMedida(
                        "${porcao.nome} (${formatarNumero(porcao.gramas)}g)",
                        ativo = edicao.porcao == porcao,
                    ) { onPorcaoChange(porcao) }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = edicao.quantidade,
                onValueChange = onQuantidadeChange,
                singleLine = true,
                label = { Text("Quantidade") },
                suffix = { Text(edicao.porcao?.nomePlural ?: "g") },
                isError = item == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = item?.let {
                    "${formatarNumero(it.quantidadeG)}g · ${it.kcal.roundToInt()} kcal · " +
                        "P ${it.proteinaG.roundToInt()}g · C ${it.carboG.roundToInt()}g · G ${it.gorduraG.roundToInt()}g"
                } ?: "Informe uma quantidade maior que zero",
                color = if (item == null) colors.error else colors.onSurfaceVariant,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(20.dp))
            BotaoPrincipal(
                texto = if (edicao.indiceItem == null) "Adicionar" else "Atualizar",
                onClick = onConfirmar,
                habilitado = item != null,
            )
        }
    }
}

@Composable
private fun ChipMedida(texto: String, ativo: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = texto,
        color = if (ativo) colors.onPrimary else colors.onBackground,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (ativo) colors.primary else colors.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Preview
@Composable
private fun RegistrarRefeicaoPreviewVazia() {
    MovvaTheme {
        RegistrarRefeicaoContent(
            state = RegistrarRefeicaoUiState(
                tipo = TipoRefeicao.ALMOCO,
                sugestoes = listOfNotNull(CatalogoAlimentos.porId("arroz-branco"), CatalogoAlimentos.porId("frango-grelhado")),
            ),
        )
    }
}

@Preview
@Composable
private fun RegistrarRefeicaoPreviewComItens() {
    val arroz = CatalogoAlimentos.porId("arroz-branco")!!
    val frango = CatalogoAlimentos.porId("frango-grelhado")!!
    val feijao = CatalogoAlimentos.porId("feijao-carioca")!!
    MovvaTheme {
        RegistrarRefeicaoContent(
            state = RegistrarRefeicaoUiState(
                tipo = TipoRefeicao.ALMOCO,
                itens = listOf(
                    arroz.item(1.0, arroz.porcoes.first()),
                    feijao.item(1.0, feijao.porcoes.first()),
                    frango.item(120.0, null),
                ),
                modificado = true,
            ),
        )
    }
}

@Preview
@Composable
private fun RegistrarRefeicaoPreviewBusca() {
    MovvaTheme {
        RegistrarRefeicaoContent(state = RegistrarRefeicaoUiState(busca = "ar"))
    }
}
