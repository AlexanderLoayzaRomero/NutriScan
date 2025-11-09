// en /app/src/main/java/bw/development/nutriscan/ui/screens/FoodLogScreen.kt
package bw.development.nutriscan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.ui.viewmodels.FoodLogViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory

sealed interface LogItem {
    data class Header(val title: String) : LogItem
    data class Food(val foodItem: FoodItem) : LogItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    onNavigateBack: () -> Unit,
    // --- AÑADIR ESTE PARÁMETRO ---
    onNavigateToEditFood: (Int) -> Unit
) {

    val viewModel: FoodLogViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )
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
        if (logItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes alimentos registrados.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logItems, key = {
                    when(it) {
                        is LogItem.Header -> "header_${it.title}"
                        is LogItem.Food -> "food_${it.foodItem.id}"
                    }
                }) { item ->
                    when (item) {
                        is LogItem.Header -> {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        is LogItem.Food -> {
                            // --- INICIO DE LA LÓGICA CORREGIDA ---
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.onDeleteItem(item.foodItem)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
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
                                FoodListItem(
                                    food = item.foodItem,
                                    onItemClick = { onNavigateToEditFood(item.foodItem.id) }
                                )
                            }
                            // --- FIN DE LÓGICA CORREGIDA ---
                        }
                    }
                }
            }
        }
    }
}