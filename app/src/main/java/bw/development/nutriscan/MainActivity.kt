package bw.development.nutriscan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bw.development.nutriscan.data.UserPreferencesRepository
import bw.development.nutriscan.ui.screens.AddFoodScreen
import bw.development.nutriscan.ui.screens.FoodLogScreen
import bw.development.nutriscan.ui.screens.HomeScreen
import bw.development.nutriscan.ui.screens.MealDetailScreen
import bw.development.nutriscan.ui.screens.OnboardingScreen
import bw.development.nutriscan.ui.screens.SettingsScreen
import bw.development.nutriscan.ui.theme.NutriScanTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bw.development.nutriscan.ui.screens.LoginScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------------------------------------------------------------
        // 1. PEDIR PERMISO DE NOTIFICACIONES (Obligatorio en Android 13+)
        // ---------------------------------------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // ---------------------------------------------------------------------
        // 2. CÓDIGO DE PRUEBA (¡SOLO PARA HOY!)
        // Esto dispara la notificación INMEDIATAMENTE al abrir la app
        // ---------------------------------------------------------------------
        val testRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(this).enqueue(testRequest)

        /*
        // ---------------------------------------------------------------------
        // 3. CÓDIGO FINAL (DESCOMENTAR PARA LA VERSIÓN REAL)
        // Esto programará la notificación para que salga cada 24 horas
        // ---------------------------------------------------------------------
        val dailyRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(5, TimeUnit.HOURS) // Espera un poco antes de la primera
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyNutriReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Si ya existe, no lo duplica
            dailyRequest
        )
        */

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

@Composable
fun NutriScanApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val prefsRepo = remember { UserPreferencesRepository(context) }
    val hasCompletedOnboarding by prefsRepo.hasCompletedOnboardingFlow.collectAsState(initial = null)

    val currentUser = Firebase.auth.currentUser

    if (hasCompletedOnboarding == null) {
        return
    }

    val startRoute = when {
        currentUser == null -> "login"
        hasCompletedOnboarding == false -> "onboarding"
        else -> "home"
    }

    NavHost(navController = navController, startDestination = startRoute) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Al loguearse exitosamente, decidimos a dónde ir
                    // Leemos de nuevo el onboarding para estar seguros o lo enviamos directo a onboarding
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

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

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                // AÑADE ESTO:
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}
}