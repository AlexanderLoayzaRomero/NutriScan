package bw.development.nutriscan.ui.theme

import androidx.compose.ui.graphics.Color

// --- PALETA VERDE NUTRISCAN ---

// Verdes Principales
val NutriPrimary = Color(0xFF2E7D32)      // Verde bosque fuerte (Botones, Títulos)
val NutriSecondary = Color(0xFF43A047)    // Verde hoja vibrante
val NutriTertiary = Color(0xFF66BB6A)     // Verde claro

// Fondos y Superficies
val NutriBackground = Color(0xFFF1F8E9)   // Verde menta muy pálido (Fondo de pantalla)
val NutriSurface = Color(0xFFFFFFFF)      // Blanco (Para algunas tarjetas)

// IMPORTANTE: Estos son los que quitan el morado
val NutriSurfaceVariant = Color(0xFFFFFFFF) // Blanco para las tarjetas (Add Food, Scan, etc.)
val NutriOnSurfaceVariant = Color(0xFF1B5E20) // Texto verde oscuro sobre las tarjetas

// Contenedores (ej. la pista gris del círculo de progreso)
val NutriSecondaryContainer = Color(0xFFC8E6C9) // Verde muy suave
val NutriOnSecondaryContainer = Color(0xFF000000)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// (Mantenemos compatibilidad por si acaso, pero no los usaremos)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)