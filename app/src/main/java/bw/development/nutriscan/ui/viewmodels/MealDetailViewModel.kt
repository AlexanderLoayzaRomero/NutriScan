// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/MealDetailViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// Asegúrate de que el constructor tenga "private val"
class MealDetailViewModel(private val foodItemDao: FoodItemDao, mealType: String) : ViewModel() {

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

    val foodItems: StateFlow<List<FoodItem>> =
        foodItemDao.getFoodItemsByCategoryForDay(mealType, todayStart, todayEnd)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    // --- ¡AQUÍ ESTÁ LA FUNCIÓN QUE FALTA! ---
    fun onDeleteItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodItemDao.deleteFoodItem(foodItem)
        }
    }
}