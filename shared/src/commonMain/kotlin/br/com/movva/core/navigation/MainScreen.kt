package br.com.movva.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.com.movva.components.BlankScreen
import br.com.movva.components.dieta.ui.DietasScreen
import br.com.movva.components.dieta.ui.RegistrarRefeicaoArgs
import br.com.movva.components.dieta.ui.RegistrarRefeicaoScreen
import br.com.movva.components.perfil.ui.PerfilScreen
import br.com.movva.components.treino.ui.NovoTreinoScreen
import br.com.movva.components.treino.ui.TreinoDetalheScreen
import br.com.movva.components.treino.ui.TreinosScreen
import br.com.movva.core.ui.EscopoDeTela
import br.com.movva.feature.home.ui.HomeScreen

@Composable
fun MainScreen(onLogout: () -> Unit) {
    var abaAtual by rememberSaveable { mutableStateOf(Aba.TREINOS) }
    var registrandoRefeicao by rememberSaveable { mutableStateOf(false) }
    var refeicaoEditadaId by rememberSaveable { mutableStateOf<String?>(null) }
    var mensagemDieta by rememberSaveable { mutableStateOf<String?>(null) }
    val emFluxoDeCriacao = abaAtual == Aba.DIETAS && registrandoRefeicao

    Scaffold(
        bottomBar = {
            if (!emFluxoDeCriacao) {
                MovvaNavBar(
                    abaAtual = abaAtual,
                    onAbaClick = { abaAtual = it },
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (abaAtual) {
                Aba.INICIO -> HomeScreen(
                    onIrParaTreinos = { abaAtual = Aba.TREINOS },
                    onIrParaDieta = { abaAtual = Aba.DIETAS },
                )
                Aba.TREINOS -> TreinosTab()
                Aba.DESAFIOS -> BlankScreen(title = abaAtual.titulo)
                Aba.DIETAS -> if (registrandoRefeicao) {
                    val fechar = {
                        registrandoRefeicao = false
                        refeicaoEditadaId = null
                    }
                    EscopoDeTela {
                        RegistrarRefeicaoScreen(
                            args = RegistrarRefeicaoArgs(dietaId = refeicaoEditadaId),
                            onVoltar = fechar,
                            onSalvo = { mensagem ->
                                mensagemDieta = mensagem
                                fechar()
                            },
                        )
                    }
                } else {
                    DietasScreen(
                        onRegistrarClick = { registrandoRefeicao = true },
                        onDietaClick = {
                            refeicaoEditadaId = it.id
                            registrandoRefeicao = true
                        },
                        mensagem = mensagemDieta,
                        onMensagemMostrada = { mensagemDieta = null },
                    )
                }
                Aba.PERFIL -> PerfilScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun TreinosTab() {
    var treinoAbertoId by rememberSaveable { mutableStateOf<String?>(null) }
    var criandoTreino by rememberSaveable { mutableStateOf(false) }

    val id = treinoAbertoId
    when {
        criandoTreino -> NovoTreinoScreen(onVoltar = { criandoTreino = false })
        id != null -> EscopoDeTela {
            TreinoDetalheScreen(treinoId = id, onVoltar = { treinoAbertoId = null })
        }
        else -> TreinosScreen(
            onTreinoClick = { treinoAbertoId = it.id },
            onNovoTreinoClick = { criandoTreino = true },
        )
    }
}
