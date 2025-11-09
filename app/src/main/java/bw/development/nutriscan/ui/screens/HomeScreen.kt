package bw.development.nutriscan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // CAMBIO: La lambda ahora recibe un booleano
    onNavigateToAddFood: (Boolean) -> Unit,
    onNavigateToMealDetail: (String) -> Unit,
    onNavigateToFoodLog: () -> Unit
) {
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            ActionCard(
                text = "AÑADIR ALIMENTO",
                icon = Icons.Default.AddCircle,
                // CAMBIO: Navega sin escanear
                onClick = { onNavigateToAddFood(false) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionCard(
                text = "ESCANEAR",
                icon = Icons.Default.QrCodeScanner,
                // CAMBIO: ¡Arreglado! Navega Y escanea
                onClick = { onNavigateToAddFood(true) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Mis Comidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            MealSection(onNavigateToMealDetail)

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

// ... (El resto del archivo: ActionCard, MealSection, etc. no cambian)
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

@Composable
fun MealSection(onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            MealItem(text = "Desayuno", icon = Icons.Default.FreeBreakfast, onClick = { onNavigate("Desayuno") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            MealItem(text = "Comida", icon = Icons.Default.Restaurant, onClick = { onNavigate("Comida") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            MealItem(text = "Cena", icon = Icons.Default.DinnerDining, onClick = { onNavigate("Cena") })
        }
    }
}

@Composable
fun MealItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 18.sp)
    }
}

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