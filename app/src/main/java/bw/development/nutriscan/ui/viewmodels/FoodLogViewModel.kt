// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/FoodLogViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Este ViewModel es muy similar al MealDetailViewModel
class FoodLogViewModel(foodItemDao: FoodItemDao) : ViewModel() {

    // Llama a la nueva función del DAO
    val allFoodItems: StateFlow<List<FoodItem>> =
        foodItemDao.getAllFoodItems() // Usamos la nueva consulta
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}