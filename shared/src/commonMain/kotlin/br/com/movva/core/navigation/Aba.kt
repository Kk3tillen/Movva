package br.com.movva.core.navigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RiceBowl
import androidx.compose.ui.graphics.vector.ImageVector

enum class Aba(val titulo: String, val icone: ImageVector) {
    INICIO("Início", Icons.Outlined.Home),
    TREINOS("Treino", Icons.Outlined.FitnessCenter),
    DIETAS("Dieta", Icons.Outlined.RiceBowl),
    DESAFIOS("Desafios", Icons.Outlined.EmojiEvents),
    PERFIL("Perfil", Icons.Outlined.Person),
}
