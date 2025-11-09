package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import kotlinx.coroutines.launch

class AddFoodViewModel(private val foodItemDao: FoodItemDao) : ViewModel() {

    fun saveFoodItem(
        name: String,
        quantity: Double,
        category: String,
        calories: Int,
        protein: Double?,
        fat: Double?,
        carbs: Double?
    ) {
        val newFoodItem = FoodItem(
            name = name,
            quantity = quantity,
            category = category,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
        viewModelScope.launch {
            foodItemDao.insertFoodItem(newFoodItem)
        }
    }
}