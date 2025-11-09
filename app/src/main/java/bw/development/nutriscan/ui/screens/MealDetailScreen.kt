// en /app/src/main/java/bw/development/nutriscan/ui/screens/MealDetailScreen.kt
package bw.development.nutriscan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// IMPORTAR EL ESTADO CORRECTO
import androidx.compose.material3.rememberSwipeToDismissBoxState
// IMPORTAR EL VALOR CORRECTO
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.ui.viewmodels.MealDetailViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    mealType: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToEditFood: (Int) -> Unit
) {
    val viewModel: MealDetailViewModel = viewModel(
        factory = ViewModelFactory(
            context = LocalContext.current.applicationContext,
            mealType = mealType
        )
    )
    val foodItems by viewModel.foodItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mealType) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFood) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Alimento")
            }
        }
    ) { paddingValues ->
        if (foodItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay alimentos registrados.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems, key = { it.id }) { food ->

                    // --- INICIO DE LA LÓGICA CORREGIDA ---
                    val dismissState = rememberSwipeToDismissBoxState(
                        // Este lambda se llama ANTES de que el estado cambie.
                        // Es el lugar perfecto para llamar al ViewModel.
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onDeleteItem(food)
                                true // Confirma el cambio (el item se "irá")
                            } else {
                                false // No confirma otros cambios (ej. deslizar a la derecha)
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        // El fondo rojo solo se muestra cuando deslizamos al inicio (EndToStart)
                        backgroundContent = {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Red)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Borrar",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        // El contenido original (el item de la comida)
                        FoodListItem(
                            food = food,
                            onItemClick = { onNavigateToEditFood(food.id) }
                        )
                    }
                    // --- FIN DE LÓGICA CORREGIDA ---
                }
            }
        }
    }
}

// ... (FoodListItem Composable sin cambios)
@Composable
fun FoodListItem(food: FoodItem, onItemClick: () -> Unit = {}) { // Añade onItemClick
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick), // Haz que la tarjeta sea clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${food.quantity}g", style = MaterialTheme.typography.bodyMedium)
            }
            Text("${food.calories} kcal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
  }