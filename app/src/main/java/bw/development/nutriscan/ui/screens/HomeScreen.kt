// en /app/src/main/java/bw/development/nutriscan/ui/screens/HomeScreen.kt
package bw.development.nutriscan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Fastfood
// --- AÑADIR ESTE IMPORT ---
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.ui.viewmodels.HomeUiState
import bw.development.nutriscan.ui.viewmodels.HomeViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory
import bw.development.nutriscan.R
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddFood: (Boolean) -> Unit,
    onNavigateToMealDetail: (String) -> Unit,
    onNavigateToFoodLog: () -> Unit,
    // --- AÑADIR ESTE PARÁMETRO ---
    onNavigateToSettings: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 1. TU NUEVO LOGO
                        Image(
                            painter = painterResource(id = R.drawable.ic_nutriscan_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape) // <--- ESTA LÍNEA ES MÁGICA: Recorta en círculo
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // 2. EL TEXTO
                        Text(
                            text = "NutriScan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Vuelve el fondo VERDE
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Texto BLANCO
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // -----------------------------
                actions = {
                    val user = Firebase.auth.currentUser

                    // Si tiene foto, la mostramos
                    if (user?.photoUrl != null) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp) // Un buen tamaño para avatar
                                .clip(CircleShape) // Redondo
                                .clickable { onNavigateToSettings() } // Al hacer clic, va a ajustes
                        )
                    } else {
                        // Si no tiene foto (o no hay usuario), mostramos la tuerca clásica
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            TodaySummaryCard(uiState = uiState) // (Sin cambios)
            Spacer(modifier = Modifier.height(16.dp))

            // ... (ActionCards, Textos, MealSection, InfoCard... todo sin cambios) ...
            ActionCard(
                text = "AÑADIR ALIMENTO",
                icon = Icons.Default.AddCircle,
                onClick = { onNavigateToAddFood(false) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionCard(
                text = "ESCANEAR",
                icon = Icons.Default.QrCodeScanner,
                onClick = { onNavigateToAddFood(true) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Mis Comidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            MealSection(onNavigateToMealDetail, uiState)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Registro Alimenticio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                text = "Registro Alimenticio",
                icon = Icons.Default.Article,
                onClick = onNavigateToFoodLog
            )
        }
    }
}

// ... (TodaySummaryCard sin cambios, excepto por el texto de la meta) ...
@Composable
fun TodaySummaryCard(uiState: HomeUiState) {
    // Degradado verde
    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Bordes más redondeados
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Tarjeta blanca limpia
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Resumen de Hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))

            val safeGoal = if (uiState.calorieGoal > 0) uiState.calorieGoal.toFloat() else 1f
            val progress = (uiState.totalCalories.toFloat() / safeGoal).coerceIn(0f, 1f)

            // Animación más lenta y suave (1.5 segundos)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1500),
                label = "CalorieProgress"
            )

            Box(contentAlignment = Alignment.Center) {
                // Fondo del progreso (gris claro)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(160.dp),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                )
                // Progreso real con degradado (Truco: Usamos el color primario pero se ve genial)
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(160.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.totalCalories}",
                        style = MaterialTheme.typography.displayMedium, // Texto más grande
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "de ${uiState.calorieGoal} kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(label = "Proteínas", value = "${uiState.totalProtein}g", color = Color(0xFF1E88E5)) // Azul
                MacroItem(label = "Grasas", value = "${uiState.totalFat}g", color = Color(0xFFE53935))     // Rojo
                MacroItem(label = "Carbos", value = "${uiState.totalCarbs}g", color = Color(0xFFFB8C00))   // Naranja
            }
        }
    }
}
@Composable
fun ActionCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // Esquinas redondeadas igual que Mis Comidas
        colors = CardDefaults.cardColors(
            // Fondo blanco limpio
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        // Borde verde suave para unificar el estilo
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con el color verde primario para que resalte
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Texto del botón
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Texto verde oscuro
            )
        }
    }
}
@Composable
fun MealSection(onNavigate: (String) -> Unit, uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Esquinas más redondeadas (coherente con el resto)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Esto ahora es BLANCO gracias a tu Theme.kt
        ),
        // Borde verde muy suave y fino para dar elegancia
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            MealItem(
                text = "Desayuno",
                icon = Icons.Default.FreeBreakfast,
                calories = uiState.breakfastCalories,
                onClick = { onNavigate("Desayuno") }
            )
            // Divisor más sutil
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MealItem(
                text = "Comida",
                icon = Icons.Default.Restaurant,
                calories = uiState.lunchCalories,
                onClick = { onNavigate("Comida") }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MealItem(
                text = "Cena",
                icon = Icons.Default.DinnerDining,
                calories = uiState.dinnerCalories,
                onClick = { onNavigate("Cena") }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            MealItem(
                text = "Snack",
                icon = Icons.Default.Fastfood,
                calories = uiState.snackCalories,
                onClick = { onNavigate("Snack") }
            )
        }
    }
}

// ... (MealItem sin cambios) ...
@Composable
fun MealItem(text: String, icon: ImageVector, calories: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 18.sp, modifier = Modifier.weight(1f))
        if (calories > 0) {
            Text(
                text = "$calories kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InfoCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}