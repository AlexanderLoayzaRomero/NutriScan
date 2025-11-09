// en /app/src/main/java/bw/development/nutriscan/ui/screens/HomeScreen.kt
package bw.development.nutriscan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
// --- NUEVOS IMPORTS ---
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
// --- NUEVO IMPORT ---
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- NUEVOS IMPORTS ---
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.ui.viewmodels.HomeUiState
import bw.development.nutriscan.ui.viewmodels.HomeViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddFood: (Boolean) -> Unit,
    onNavigateToMealDetail: (String) -> Unit,
    onNavigateToFoodLog: () -> Unit
) {
    // 1. Inicializa el ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )

    // 2. Observa el estado del UI
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NutriScan", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // 3. Añadimos un Scroll por si el contenido crece
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // --- NUEVO COMPOSABLE: El Dashboard de Resumen ---
            TodaySummaryCard(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Botones de Acción (sin cambios, solo se pasa el 'false') ---
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

            // 4. Pasamos el uiState a la sección de comidas
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

// --- NUEVO COMPOSABLE COMPLETO ---
@Composable
fun TodaySummaryCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Resumen de Hoy", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Calcula el progreso
            val progress = (uiState.totalCalories.toFloat() / uiState.calorieGoal.toFloat()).coerceIn(0f, 1f)
            // Anima el progreso
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                label = "CalorieProgress"
            )

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.totalCalories}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ ${uiState.calorieGoal} kcal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ... ActionCard (sin cambios) ...
@Composable
fun ActionCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- COMPOSABLE MODIFICADO ---
@Composable
fun MealSection(onNavigate: (String) -> Unit, uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            MealItem(
                text = "Desayuno",
                icon = Icons.Default.FreeBreakfast,
                calories = uiState.breakfastCalories, // Pasa las calorías
                onClick = { onNavigate("Desayuno") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            MealItem(
                text = "Comida",
                icon = Icons.Default.Restaurant,
                calories = uiState.lunchCalories, // Pasa las calorías
                onClick = { onNavigate("Comida") }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            MealItem(
                text = "Cena",
                icon = Icons.Default.DinnerDining,
                calories = uiState.dinnerCalories, // Pasa las calorías
                onClick = { onNavigate("Cena") }
            )
            // Opcional: Añadir "Snacks" si quieres que aparezca aquí
        }
    }
}

// --- COMPOSABLE MODIFICADO ---
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
        // Muestra las calorías
        if (calories > 0) {
            Text(
                text = "$calories kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ... InfoCard (sin cambios) ...
@Composable
fun InfoCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontSize = 18.sp)
        }
    }
}