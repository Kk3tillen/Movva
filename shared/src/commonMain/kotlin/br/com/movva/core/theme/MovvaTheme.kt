package br.com.movva.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OrangeDark = Color(0xFFF5A623)
private val OrangeLight = Color(0xFFE34200)

private val MovvaDarkColors = darkColorScheme(
    primary = OrangeDark,
    onPrimary = Color.Black,
    background = Color(0xFF161616),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF1F1F1F),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFF7A7A7A),
    outline = Color(0xFF2E2E2E),
    error = Color(0xFFE5484D),
)

private val MovvaLightColors = lightColorScheme(
    primary = OrangeLight,
    onPrimary = Color.White,
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFF2F0F4),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFF2F0F4),
    onSurfaceVariant = Color(0xFF6F6F6F),
    outline = Color(0xFFD8D2DC),
    error = Color(0xFFBA1A1A),
)

@Composable
fun MovvaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MovvaDarkColors else MovvaLightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
