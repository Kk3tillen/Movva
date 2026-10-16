package br.com.movva.components.dieta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.domain.Alimento
import br.com.movva.components.dieta.domain.CatalogoAlimentos
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.dieta.domain.ItemRefeicao
import br.com.movva.components.dieta.domain.Porcao
import br.com.movva.components.dieta.domain.TipoRefeicao
import br.com.movva.components.dieta.domain.formatarNumero
import br.com.movva.components.dieta.domain.paraQuantidade
import br.com.movva.components.dieta.domain.totais
import br.com.movva.core.util.horaAtual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// dietaId != null abre em modo edição; tipoInicial pré-seleciona o chip (senão, pelo horário).
data class RegistrarRefeicaoArgs(
    val dietaId: String? = null,
    val tipoInicial: TipoRefeicao? = null,
)

// Escolha de quantidade/porção de um alimento, antes de adicioná-lo (ou ao editar um item).
data class EdicaoQuantidade(
    val alimento: Alimento,
    val indiceItem: Int?,
    val porcao: Porcao?,
    val quantidade: String,
) {
    val item: ItemRefeicao? get() = quantidade.paraQuantidade()?.let { alimento.item(it, porcao) }
}

data class RegistrarRefeicaoUiState(
    val carregando: Boolean = false,
    val erroCarregar: String? = null,
    val modoEdicao: Boolean = false,
    val tipo: TipoRefeicao = TipoRefeicao.LANCHE,
    val busca: String = "",
    val sugestoes: List<Alimento> = emptyList(),
    val itens: List<ItemRefeicao> = emptyList(),
    val edicao: EdicaoQuantidade? = null,
    val modificado: Boolean = false,
    val salvando: Boolean = false,
    // refeição do mesmo tipo já registrada hoje: pergunta se soma ou cria outra
    val conflito: Dieta? = null,
    // preenchida depois de salvar; a tela volta para a lista mostrando a mensagem
    val mensagemSalvo: String? = null,
    val erro: String? = null,
) {
    val resultados: List<Alimento> get() = CatalogoAlimentos.buscar(busca)
    val totais get() = itens.totais()
    val podeSalvar: Boolean get() = itens.isNotEmpty() && !salvando
    val temAlteracaoNaoSalva: Boolean get() = modificado && itens.isNotEmpty()
}

private val SUGESTOES_PADRAO = listOf("arroz-branco", "feijao-carioca", "frango-grelhado", "ovo-cozido", "banana-prata", "pao-frances")

class RegistrarRefeicaoViewModel(
    private val args: RegistrarRefeicaoArgs,
    private val dietaRepository: DietaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrarRefeicaoUiState(
            modoEdicao = args.dietaId != null,
            carregando = args.dietaId != null,
            tipo = args.tipoInicial ?: TipoRefeicao.peloHorario(horaAtual()),
        )
    )
    val uiState: StateFlow<RegistrarRefeicaoUiState> = _uiState.asStateFlow()

    init {
        carregarSugestoes()
        args.dietaId?.let(::carregarRefeicao)
    }

    private fun carregarSugestoes() {
        viewModelScope.launch {
            val ids = (dietaRepository.getAlimentosFrequentes(limite = 6) + SUGESTOES_PADRAO).distinct().take(6)
            _uiState.update { it.copy(sugestoes = ids.mapNotNull(CatalogoAlimentos::porId)) }
        }
    }

    fun carregarRefeicao(id: String) {
        _uiState.update { it.copy(carregando = true, erroCarregar = null) }
        viewModelScope.launch {
            dietaRepository.getDieta(id)
                .onSuccess { dieta ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            tipo = TipoRefeicao.doTitulo(dieta.tipoRefeicao) ?: it.tipo,
                            itens = dieta.itens,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(carregando = false, erroCarregar = e.message ?: "Não foi possível carregar a refeição")
                    }
                }
        }
    }

    fun onTipoChange(tipo: TipoRefeicao) = _uiState.update {
        if (it.tipo == tipo) it else it.copy(tipo = tipo, modificado = true)
    }

    fun onBuscaChange(texto: String) = _uiState.update { it.copy(busca = texto) }

    // começa pela primeira medida caseira (1 unidade/xícara...); sem medida, 100 g
    fun onAlimentoClick(alimento: Alimento) = _uiState.update {
        val porcao = alimento.porcoes.firstOrNull()
        it.copy(edicao = EdicaoQuantidade(alimento, null, porcao, if (porcao == null) "100" else "1"))
    }

    fun onItemClick(indice: Int) = _uiState.update { state ->
        val item = state.itens.getOrNull(indice) ?: return@update state
        val alimento = CatalogoAlimentos.porId(item.alimentoId) ?: return@update state
        val porcao = alimento.porcao(item.porcaoNome)
        val quantidade = if (porcao != null && item.quantidadePorcao != null) item.quantidadePorcao else item.quantidadeG
        state.copy(edicao = EdicaoQuantidade(alimento, indice, porcao, formatarNumero(quantidade)))
    }

    // ao trocar de medida, converte a quantidade para manter o mesmo peso
    fun onPorcaoChange(porcao: Porcao?) = _uiState.update { state ->
        val edicao = state.edicao ?: return@update state
        val gramas = edicao.item?.quantidadeG
        val quantidade = when {
            gramas == null -> edicao.quantidade
            porcao == null -> formatarNumero(gramas)
            else -> formatarNumero(gramas / porcao.gramas)
        }
        state.copy(edicao = edicao.copy(porcao = porcao, quantidade = quantidade))
    }

    fun onQuantidadeChange(texto: String) = _uiState.update { state ->
        val filtrado = texto.filter { it.isDigit() || it == ',' || it == '.' }.take(6)
        state.copy(edicao = state.edicao?.copy(quantidade = filtrado))
    }

    fun onConfirmarQuantidade() = _uiState.update { state ->
        val edicao = state.edicao ?: return@update state
        val item = edicao.item ?: return@update state
        val itens = when (val i = edicao.indiceItem) {
            null -> state.itens + item
            else -> state.itens.mapIndexed { idx, atual -> if (idx == i) item else atual }
        }
        // depois de adicionar, limpa a busca para o próximo alimento
        state.copy(itens = itens, edicao = null, busca = if (edicao.indiceItem == null) "" else state.busca, modificado = true)
    }

    fun onCancelarQuantidade() = _uiState.update { it.copy(edicao = null) }

    fun onRemoverItem(indice: Int) = _uiState.update {
        it.copy(itens = it.itens.filterIndexed { i, _ -> i != indice }, modificado = true)
    }

    fun salvar() {
        val state = _uiState.value
        if (!state.podeSalvar) return

        if (state.modoEdicao) {
            salvarItens(args.dietaId, state.itens, "${state.tipo.titulo} atualizado")
            return
        }
        viewModelScope.launch {
            val existente = dietaRepository.getRefeicaoDeHoje(state.tipo.titulo)
            if (existente != null) {
                _uiState.update { it.copy(conflito = existente) }
            } else {
                salvarItens(null, state.itens, "${state.tipo.titulo} registrado")
            }
        }
    }

    fun onSomarAExistente() {
        val state = _uiState.value
        val existente = state.conflito ?: return
        _uiState.update { it.copy(conflito = null) }
        salvarItens(existente.id, existente.itens + state.itens, "Alimentos somados ao ${state.tipo.titulo}")
    }

    fun onCriarOutra() {
        val state = _uiState.value
        _uiState.update { it.copy(conflito = null) }
        salvarItens(null, state.itens, "${state.tipo.titulo} registrado")
    }

    fun onFecharConflito() = _uiState.update { it.copy(conflito = null) }

    private fun salvarItens(id: String?, itens: List<ItemRefeicao>, mensagem: String) {
        _uiState.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            dietaRepository.salvarRefeicao(id, _uiState.value.tipo.titulo, itens)
                .onSuccess { _uiState.update { it.copy(salvando = false, modificado = false, mensagemSalvo = mensagem) } }
                .onFailure { e ->
                    _uiState.update { it.copy(salvando = false, erro = e.message ?: "Não foi possível salvar a refeição") }
                }
        }
    }
}
