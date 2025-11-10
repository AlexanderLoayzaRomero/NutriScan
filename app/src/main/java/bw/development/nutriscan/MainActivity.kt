// en /app/src/main/java/bw/development/nutriscan/MainActivity.kt
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bw.development.nutriscan.ui.screens.AddFoodScreen
import bw.development.nutriscan.ui.screens.FoodLogScreen
import bw.development.nutriscan.ui.screens.HomeScreen
import bw.development.nutriscan.ui.screens.MealDetailScreen
// --- AÑADIR ESTE IMPORT ---
import bw.development.nutriscan.ui.screens.SettingsScreen
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

        // 1. Pantalla principal (Home) - MODIFICADA
        composable("home") {
            HomeScreen(
                onNavigateToAddFood = { shouldScan ->
                    navController.navigate("addFood?startScan=$shouldScan&itemId=0")
                },
                onNavigateToMealDetail = { mealType ->
                    navController.navigate("mealDetail/$mealType")
                },
                onNavigateToFoodLog = { navController.navigate("foodLog") },
                // --- AÑADIR ESTA LÍNEA ---
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // 2. Pantalla para añadir alimento (sin cambios)
        composable(
            route = "addFood?startScan={startScan}&itemId={itemId}",
            arguments = listOf(
                navArgument("startScan") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("itemId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val startScan = backStackEntry.arguments?.getBoolean("startScan") ?: false
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0

            AddFoodScreen(
                onNavigateBack = { navController.popBackStack() },
                startScanNow = startScan,
                editingItemId = itemId
            )
        }

        // 3. Pantalla de detalle de comida (sin cambios)
        composable("mealDetail/{mealType}") { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Comida"
            MealDetailScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate("addFood?startScan=false&itemId=0") },
                onNavigateToEditFood = { itemId ->
                    navController.navigate("addFood?startScan=false&itemId=$itemId")
                }
            )
        }

        // 4. Pantalla de registro alimenticio (sin cambios)
        composable("foodLog") {
            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditFood = { itemId ->
                    navController.navigate("addFood?startScan=false&itemId=$itemId")
                }
            )
        }

        // --- 5. AÑADIR NUEVA RUTA DE AJUSTES ---
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}