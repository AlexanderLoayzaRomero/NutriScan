// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/HomeViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItemDao
// --- AÑADIR IMPORT ---
import bw.development.nutriscan.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
// --- AÑADIR IMPORT ---
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val totalCalories: Int = 0,
    val breakfastCalories: Int = 0,
    val lunchCalories: Int = 0,
    val dinnerCalories: Int = 0,
    val snackCalories: Int = 0,
    // --- CAMBIO: La meta ahora viene del estado ---
    val calorieGoal: Int = 0
)

// --- CAMBIO: El constructor ahora pide el UserPreferencesRepository ---
class HomeViewModel(
    foodItemDao: FoodItemDao,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val todayStart: Long
    private val todayEnd: Long

    init {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        todayStart = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        todayEnd = cal.timeInMillis
    }

    // --- CAMBIO: Combinamos dos Flows (comidas y meta) ---
    val uiState: StateFlow<HomeUiState> =
        // 1. Obtenemos el flow de la meta de calorías
        userPreferencesRepository.calorieGoalFlow
            .combine(
                // 2. Obtenemos el flow de las comidas de hoy
                foodItemDao.getAllFoodItemsForDay(todayStart, todayEnd)
            ) { goal, items -> // 3. El bloque 'combine' se ejecuta cuando CUALQUIERA de los dos cambia

                var total = 0
                var breakfast = 0
                var lunch = 0
                var dinner = 0
                var snack = 0

                items.forEach {
                    total += it.calories
                    when (it.category) {
                        "Desayuno" -> breakfast += it.calories
                        "Comida" -> lunch += it.calories
                        "Cena" -> dinner += it.calories
                        "Snack" -> snack += it.calories
                    }
                }

                HomeUiState(
                    totalCalories = total,
                    breakfastCalories = breakfast,
                    lunchCalories = lunch,
                    dinnerCalories = dinner,
                    snackCalories = snack,
                    calorieGoal = goal // 4. Usamos la meta de calorías leída del DataStore
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = HomeUiState()
            )
}