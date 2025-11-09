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
// AÑADIR ESTOS IMPORTS
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NutriScanApp()
                }
            }
        }
    }
}

@Composable
fun NutriScanApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        // 1. Pantalla principal (Home)
        composable("home") {
            HomeScreen(
                // CAMBIO: La lambda ahora pasa un booleano
                onNavigateToAddFood = { shouldScan ->
                    // Navega a la ruta con el argumento
                    navController.navigate("addFood?startScan=$shouldScan")
                },
                onNavigateToMealDetail = { mealType ->
                    navController.navigate("mealDetail/$mealType")
                },
                onNavigateToFoodLog = { navController.navigate("foodLog") }
            )
        }

        // 2. Pantalla para añadir alimento (AHORA CON ARGUMENTO)
        composable(
            // 1. Definir la ruta con el argumento opcional
            route = "addFood?startScan={startScan}",
            arguments = listOf(
                navArgument("startScan") {
                    type = NavType.BoolType
                    defaultValue = false // Por defecto, no empezar escaneando
                }
            )
        ) { backStackEntry ->
            // 2. Extraer el argumento
            val startScan = backStackEntry.arguments?.getBoolean("startScan") ?: false
            AddFoodScreen(
                onNavigateBack = { navController.popBackStack() },
                // 3. Pasar el argumento a la pantalla
                startScanNow = startScan
            )
        }

        // 3. Pantalla de detalle de comida (sin cambios)
        composable("mealDetail/{mealType}") { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Comida"
            MealDetailScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate("addFood?startScan=false") } // Aseguramos que no escanee
            )
        }

        // 4. Pantalla de registro alimenticio (sin cambios)
        composable("foodLog") {
            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}