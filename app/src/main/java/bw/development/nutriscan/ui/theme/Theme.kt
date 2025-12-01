package bw.development.nutriscan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NutriPrimary,
    secondary = NutriSecondary,
    tertiary = NutriTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = NutriPrimary,
    onPrimary = White,
    secondary = NutriSecondary,
    onSecondary = White,
    tertiary = NutriTertiary,

    // El fondo general de la pantalla
    background = NutriBackground,
    onBackground = Black,

    // Superficies estándar
    surface = NutriSurface,
    onSurface = Black,

    // --- AQUÍ ESTÁ EL TRUCO PARA QUITAR EL MORADO ---
    // Esto cambia el color de fondo de las Cards (Tarjetas) por defecto
    surfaceVariant = NutriSurfaceVariant, // Ahora será Blanco
    onSurfaceVariant = NutriOnSurfaceVariant, // Texto verde oscuro

    // Esto cambia el color de fondo de los círculos de progreso y chips
    secondaryContainer = NutriSecondaryContainer,
    onSecondaryContainer = NutriOnSecondaryContainer,

    // Bordes
    outline = NutriPrimary.copy(alpha = 0.5f)
)

@Composable
fun NutriScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANTE: dynamicColor en FALSE para que no tome colores azules de tu Android
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}