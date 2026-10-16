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
import br.com.movva.components.perfil.ui.PerfilScreen
import br.com.movva.components.treino.ui.TreinosScreen
import br.com.movva.feature.home.ui.HomeScreen

@Composable
fun MainScreen(onLogout: () -> Unit) {
    var abaAtual by rememberSaveable { mutableStateOf(Aba.TREINOS) }

    Scaffold(
        bottomBar = {
            MovvaNavBar(
                abaAtual = abaAtual,
                onAbaClick = { abaAtual = it },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (abaAtual) {
                Aba.INICIO -> HomeScreen()
                Aba.TREINOS -> TreinosScreen()
                Aba.DESAFIOS -> BlankScreen(title = abaAtual.titulo)
                Aba.DIETAS -> DietasScreen()
                Aba.PERFIL -> PerfilScreen(onLogout = onLogout)
            }
        }
    }
}
