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
                onNavigateToAddFood = { shouldScan ->
                    navController.navigate("addFood?startScan=$shouldScan&itemId=0")
                },
                onNavigateToMealDetail = { mealType ->
                    navController.navigate("mealDetail/$mealType")
                },
                onNavigateToFoodLog = { navController.navigate("foodLog") }
            )
        }

        // 2. Pantalla para añadir alimento (RUTA MODIFICADA)
        composable(
            // CAMBIO: Añadido "&itemId={itemId}"
            route = "addFood?startScan={startScan}&itemId={itemId}",
            arguments = listOf(
                navArgument("startScan") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                // CAMBIO: Añadido navArgument para itemId
                navArgument("itemId") {
                    type = NavType.IntType
                    defaultValue = 0 // 0 significa "item nuevo"
                }
            )
        ) { backStackEntry ->
            val startScan = backStackEntry.arguments?.getBoolean("startScan") ?: false
            // CAMBIO: Extraer el itemId
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0

            AddFoodScreen(
                onNavigateBack = { navController.popBackStack() },
                startScanNow = startScan,
                // CAMBIO: Pasar el itemId a la pantalla
                editingItemId = itemId
            )
        }

        // 3. Pantalla de detalle de comida (MODIFICADA)
        composable("mealDetail/{mealType}") { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Comida"
            MealDetailScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() },
                // CAMBIO: Aseguramos que itemId sea 0
                onNavigateToAddFood = { navController.navigate("addFood?startScan=false&itemId=0") },
                // CAMBIO: Añadida la navegación para editar
                onNavigateToEditFood = { itemId ->
                    navController.navigate("addFood?startScan=false&itemId=$itemId")
                }
            )
        }

        // 4. Pantalla de registro alimenticio (MODIFICADA)
        composable("foodLog") {
            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() },
                // CAMBIO: Añadida la navegación para editar
                onNavigateToEditFood = { itemId ->
                    navController.navigate("addFood?startScan=false&itemId=$itemId")
                }
            )
        }
    }
}