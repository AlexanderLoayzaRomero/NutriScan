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
import bw.development.nutriscan.ui.viewmodels.FoodLogViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(onNavigateBack: () -> Unit) {

    // 1. Inicializa el ViewModel
    val viewModel: FoodLogViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )

    // 2. Observa el StateFlow
    val foodItems by viewModel.allFoodItems.collectAsState()

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
        // 3. Comprueba si la lista está vacía
        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                // Mensaje si no hay historial
                Text("Aún no tienes alimentos registrados.")
            }
        } else {
            // 4. Muestra la lista
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems) { food ->
                    // 5. Reutilizamos el Composable de MealDetailScreen
                    // (Es público, así que podemos importarlo)
                    FoodListItem(food)
                }
            }
        }
    }
}