package bw.development.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bw.development.nutriscan.ui.screens.AddFoodScreen
import bw.development.nutriscan.ui.screens.FoodLogScreen
import bw.development.nutriscan.ui.screens.HomeScreen
import bw.development.nutriscan.ui.screens.MealDetailScreen
import bw.development.nutriscan.ui.theme.NutriScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriScanTheme {
                // Contenedor principal de la aplicación, usando el color de fondo del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NutriScanApp() // Llama a la función que contiene el NavHost
                }
            }
        }
    }
}

/**
 * Componente raíz que maneja el gráfico de navegación (NavHost).
 */
@Composable
fun NutriScanApp() {
    val navController = rememberNavController()

    // NavHost define el gráfico de navegación
    NavHost(navController = navController, startDestination = "home") {

        // 1. Pantalla principal (Home)
        composable("home") {
            HomeScreen(
                onNavigateToAddFood = { navController.navigate("addFood") },
                // La ruta incluye el argumento 'mealType'
                onNavigateToMealDetail = { mealType ->
                    navController.navigate("mealDetail/$mealType")
                },
                onNavigateToFoodLog = { navController.navigate("foodLog") }
            )
        }

        // 2. Pantalla para añadir alimento
        composable("addFood") {
            AddFoodScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Pantalla de detalle de comida (con argumento de ruta)
        composable("mealDetail/{mealType}") { backStackEntry ->
            // Se extrae el argumento 'mealType' de la ruta
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Comida"
            MealDetailScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate("addFood") }
            )
        }

        // 4. Pantalla de registro alimenticio
        composable("foodLog") {
            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
