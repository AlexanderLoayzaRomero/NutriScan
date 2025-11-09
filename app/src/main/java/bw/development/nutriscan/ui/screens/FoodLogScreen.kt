// en /app/src/main/java/bw/development/nutriscan/ui/screens/FoodLogScreen.kt
package bw.development.nutriscan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// AÑADIR ESTE IMPORT
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.ui.viewmodels.FoodLogViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory

// --- AÑADIR ESTA INTERFAZ ---
// Define los tipos de items en nuestra lista:
// O es un Encabezado (Header) o es un Alimento (Food).
sealed interface LogItem {
    data class Header(val title: String) : LogItem
    data class Food(val foodItem: FoodItem) : LogItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(onNavigateBack: () -> Unit) {

    val viewModel: FoodLogViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )

    // --- CAMBIO: Observamos la nueva lista agrupada ---
    val logItems by viewModel.groupedFoodItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Alimenticio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (logItems.isEmpty()) { // <-- Usa la nueva variable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes alimentos registrados.")
            }
        } else {
            // --- CAMBIO: LazyColumn ahora maneja dos tipos de items ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                // Padding horizontal para toda la lista
                contentPadding = PaddingValues(horizontal = 16.dp),
                // Espacio entre todos los items (headers y food)
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logItems) { item -> // <-- Itera sobre la lista sellada
                    // Usamos 'when' para decidir qué Composable mostrar
                    when (item) {
                        is LogItem.Header -> {
                            // Este es nuestro encabezado de fecha
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                // Damos un espacio extra arriba del encabezado
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        is LogItem.Food -> {
                            // Este es nuestro item de comida
                            // ¡Reutilizamos el Composable que ya teníamos!
                            FoodListItem(item.foodItem)
                        }
                    }
                }
            }
        }
    }
}