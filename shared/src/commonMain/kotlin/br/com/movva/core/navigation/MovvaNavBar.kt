package br.com.movva.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MovvaNavBar(
    abaAtual: Aba,
    onAbaClick: (Aba) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column {
        HorizontalDivider(thickness = 1.dp, color = colors.outline)
        NavigationBar(
            containerColor = colors.background,
            tonalElevation = 0.dp,
        ) {
            Aba.entries.forEach { aba ->
                val selecionada = aba == abaAtual
                NavigationBarItem(
                    selected = selecionada,
                    onClick = { onAbaClick(aba) },
                    icon = {
                        Icon(
                            imageVector = aba.icone,
                            contentDescription = aba.titulo,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                    label = {
                        Text(
                            text = aba.titulo,
                            fontSize = 12.sp,
                            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}
