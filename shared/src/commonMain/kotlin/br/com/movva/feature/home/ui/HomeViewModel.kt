package br.com.movva.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.movva.auth.AuthRepository
import br.com.movva.components.dieta.data.DietaRepository
import br.com.movva.components.dieta.data.DietasResultado
import br.com.movva.components.dieta.domain.Dieta
import br.com.movva.components.treino.data.TreinoRepository
import br.com.movva.components.treino.data.TreinosResultado
import br.com.movva.core.util.dataDeHojeIso
import br.com.movva.feature.checkin.data.CheckInRepository
import br.com.movva.feature.home.domain.mesAnteriorDe
import br.com.movva.feature.home.domain.montarCalendarioMes
import br.com.movva.feature.home.domain.proximoMesDe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TreinoHojeResumo(
    val feitoHoje: Boolean,
    val nomesTreinos: List<String> = emptyList(),
)

data class DietaHojeResumo(
    val dietaSeguidaHoje: Boolean,
    val refeicoesFeitas: Int = 0,
    val totalRefeicoes: Int = 0,
    val kcalFeitas: Int = 0,
    val kcalTotal: Int = 0,
)

sealed interface HomeUiState {
    data object Carregando : HomeUiState
    data class Vazio(
        val nomeUsuario: String,
        val streakDias: Int,
        val calendario: CalendarioUiState,
        val avisoCache: String? = null,
    ) : HomeUiState
    data class Sucesso(
        val nomeUsuario: String,
        val streakDias: Int,
        val treinoHoje: TreinoHojeResumo,
        val dietaHoje: DietaHojeResumo,
        val calendario: CalendarioUiState,
        val avisoCache: String? = null,
    ) : HomeUiState
    data class Erro(val mensagem: String) : HomeUiState
}

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val checkInRepository: CheckInRepository,
    private val treinoRepository: TreinoRepository,
    private val dietaRepository: DietaRepository,
    private val hojeProvider: () -> String = { dataDeHojeIso() },
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Carregando)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var anoAtual: Int = 0
    private var mesAtual: Int = 0

    init {
        inicializarMesAtual()
    }

    private fun inicializarMesAtual() {
        val partes = hojeProvider().split("-")
        anoAtual = partes.getOrNull(0)?.toIntOrNull() ?: 2026
        mesAtual = partes.getOrNull(1)?.toIntOrNull() ?: 1
    }

    fun carregar() {
        if (anoAtual == 0 || mesAtual == 0) inicializarMesAtual()
        viewModelScope.launch {
            _uiState.value = HomeUiState.Carregando
            try {
                val hoje = hojeProvider()
                val nome = authRepository.currentUser()?.nome ?: ""
                val streak = checkInRepository.getStreakAtual()
                val checkInHoje = checkInRepository.getCheckInDeHoje()
                val calendarioResultado = carregarEstadoCalendario(anoAtual, mesAtual, hoje)

                val treinosRes = treinoRepository.getTreinos().first()
                val dietasRes = dietaRepository.getDietas().first()

                if (treinosRes is TreinosResultado.Erro && dietasRes is DietasResultado.Erro) {
                    _uiState.value = HomeUiState.Erro(treinosRes.mensagem)
                    return@launch
                }

                val treinos = (treinosRes as? TreinosResultado.Sucesso)?.treinos ?: emptyList()
                val dietas = (dietasRes as? DietasResultado.Sucesso)?.dietas ?: emptyList()

                val avisoCache = when (calendarioResultado) {
                    is CalendarioUiState.Sucesso -> calendarioResultado.avisoCache
                    is CalendarioUiState.Vazio -> calendarioResultado.avisoCache
                    else -> null
                }

                val semDados = treinos.isEmpty() &&
                    dietas.isEmpty() &&
                    checkInHoje == null &&
                    calendarioResultado is CalendarioUiState.Vazio

                if (semDados) {
                    _uiState.value = HomeUiState.Vazio(
                        nomeUsuario = nome,
                        streakDias = streak,
                        calendario = calendarioResultado,
                        avisoCache = avisoCache,
                    )
                    return@launch
                }

                val treinosFeitosHoje = treinos.filter { it.ultimaExecucao == hoje }
                val treinoResumo = TreinoHojeResumo(
                    feitoHoje = (checkInHoje?.treinoFeito == true) || treinosFeitosHoje.isNotEmpty(),
                    nomesTreinos = treinosFeitosHoje.map { it.nome },
                )

                val dietaResumo = montarResumoDieta(
                    dietas = dietas,
                    dietaSeguidaCheckIn = checkInHoje?.dietaSeguida == true,
                )

                _uiState.value = HomeUiState.Sucesso(
                    nomeUsuario = nome,
                    streakDias = streak,
                    treinoHoje = treinoResumo,
                    dietaHoje = dietaResumo,
                    calendario = calendarioResultado,
                    avisoCache = avisoCache,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Erro(
                    e.message ?: "Não foi possível carregar os dados da tela inicial."
                )
            }
        }
    }

    fun mesAnterior() {
        val (novoAno, novoMes) = mesAnteriorDe(anoAtual, mesAtual)
        anoAtual = novoAno
        mesAtual = novoMes
        recarregarCalendario()
    }

    fun proximoMes() {
        val (novoAno, novoMes) = proximoMesDe(anoAtual, mesAtual)
        anoAtual = novoAno
        mesAtual = novoMes
        recarregarCalendario()
    }

    fun recarregarCalendario() {
        val estadoAtual = _uiState.value
        if (estadoAtual !is HomeUiState.Sucesso && estadoAtual !is HomeUiState.Vazio) {
            carregar()
            return
        }

        atualizarCalendarioNoEstado(CalendarioUiState.Carregando(anoAtual, mesAtual))
        viewModelScope.launch {
            val novoCalendario = carregarEstadoCalendario(anoAtual, mesAtual, hojeProvider())
            atualizarCalendarioNoEstado(novoCalendario)
        }
    }

    private suspend fun carregarEstadoCalendario(
        ano: Int,
        mes: Int,
        hoje: String,
    ): CalendarioUiState {
        return try {
            val res = checkInRepository.getCheckInsDoMes(ano, mes)
            if (res.erroRede != null && res.checkIns.isEmpty()) {
                return CalendarioUiState.Erro(ano, mes, res.erroRede)
            }
            val cal = montarCalendarioMes(
                ano = ano,
                mes = mes,
                hojeIso = hoje,
                checkIns = res.checkIns,
            )
            if (cal.temRegistros) {
                CalendarioUiState.Sucesso(cal, avisoCache = res.erroRede)
            } else {
                CalendarioUiState.Vazio(cal, avisoCache = res.erroRede)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CalendarioUiState.Erro(
                ano = ano,
                mes = mes,
                mensagem = e.message ?: "Não foi possível carregar o calendário.",
            )
        }
    }

    private fun atualizarCalendarioNoEstado(calendario: CalendarioUiState) {
        val aviso = when (calendario) {
            is CalendarioUiState.Sucesso -> calendario.avisoCache
            is CalendarioUiState.Vazio -> calendario.avisoCache
            else -> null
        }
        _uiState.value = when (val atual = _uiState.value) {
            is HomeUiState.Sucesso -> atual.copy(calendario = calendario, avisoCache = aviso)
            is HomeUiState.Vazio -> atual.copy(calendario = calendario, avisoCache = aviso)
            else -> atual
        }
    }

    private fun montarResumoDieta(
        dietas: List<Dieta>,
        dietaSeguidaCheckIn: Boolean,
    ): DietaHojeResumo {
        val feitas = dietas.filter { it.feitaHoje }
        val kcalFeitas = feitas.sumOf { it.totais.kcal }.roundToInt()
        val kcalTotal = dietas.sumOf { it.totais.kcal }.roundToInt()
        return DietaHojeResumo(
            dietaSeguidaHoje = dietaSeguidaCheckIn || feitas.isNotEmpty(),
            refeicoesFeitas = feitas.size,
            totalRefeicoes = dietas.size,
            kcalFeitas = kcalFeitas,
            kcalTotal = kcalTotal,
        )
    }
}
