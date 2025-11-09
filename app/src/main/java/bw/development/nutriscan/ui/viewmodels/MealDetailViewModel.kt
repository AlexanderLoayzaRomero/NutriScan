// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/MealDetailViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
// AÑADIR ESTE IMPORT
import java.util.Calendar

class MealDetailViewModel(foodItemDao: FoodItemDao, mealType: String) : ViewModel() {

    // Lógica para obtener el inicio y fin del día de HOY
    private val todayStart: Long
    private val todayEnd: Long

    init {
        val cal = Calendar.getInstance()

        // Pone la hora al inicio del día (00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        todayStart = cal.timeInMillis

        // Pone la hora al final del día (23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        todayEnd = cal.timeInMillis
    }

    // Ahora, foodItems solo contendrá los items de HOY
    val foodItems: StateFlow<List<FoodItem>> =
        // Llama a la nueva consulta del DAO
        foodItemDao.getFoodItemsByCategoryForDay(mealType, todayStart, todayEnd)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}