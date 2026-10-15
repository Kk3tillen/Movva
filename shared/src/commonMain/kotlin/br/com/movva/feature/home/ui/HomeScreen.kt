package br.com.movva.feature.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.movva.feature.checkin.ui.CheckInCard
import br.com.movva.feature.checkin.ui.CheckInViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    checkInViewModel: CheckInViewModel = koinViewModel(),
) {
    val state by checkInViewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = "Início",
                color = colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckInCard(
                state = state,
                onTreinoFeitoChange = checkInViewModel::onTreinoFeitoChange,
                onDietaSeguidaChange = checkInViewModel::onDietaSeguidaChange,
                onRegistrar = checkInViewModel::registrarCheckIn,
            )
        }
    }
}
