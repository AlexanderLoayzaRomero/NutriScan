// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/HomeViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

// 1. Definimos el estado que la HomeScreen va a observar
data class HomeUiState(
    val totalCalories: Int = 0,
    val breakfastCalories: Int = 0,
    val lunchCalories: Int = 0,
    val dinnerCalories: Int = 0,
    val snackCalories: Int = 0,
    val calorieGoal: Int = 2000 // Objetivo de calorías (puedes cambiarlo)
)

class HomeViewModel(foodItemDao: FoodItemDao) : ViewModel() {

    private val todayStart: Long
    private val todayEnd: Long

    init {
        // Obtenemos los límites del día (igual que en MealDetailViewModel)
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

    // 2. Creamos el StateFlow
    val uiState: StateFlow<HomeUiState> =
        foodItemDao.getAllFoodItemsForDay(todayStart, todayEnd) // Usamos la nueva consulta
            .map { items -> // 3. Transformamos la lista de comidas en el HomeUiState
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
                    snackCalories = snack
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = HomeUiState() // Estado inicial (todo en 0)
            )
}