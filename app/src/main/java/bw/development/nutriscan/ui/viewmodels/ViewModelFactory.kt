package bw.development.nutriscan.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bw.development.nutriscan.data.AppDatabase
import bw.development.nutriscan.data.UserPreferencesRepository

class ViewModelFactory(private val context: Context, private val mealType: String? = null) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(context)
        val prefsRepository = UserPreferencesRepository(context)

        return when {
            modelClass.isAssignableFrom(AddFoodViewModel::class.java) -> {
                AddFoodViewModel(db.foodItemDao()) as T
            }
            modelClass.isAssignableFrom(MealDetailViewModel::class.java) -> {
                requireNotNull(mealType) { "mealType must be provided for MealDetailViewModel" }
                MealDetailViewModel(db.foodItemDao(), mealType) as T
            }
            modelClass.isAssignableFrom(FoodLogViewModel::class.java) -> {
                FoodLogViewModel(db.foodItemDao()) as T
            }
            // --- AÑADIR ESTE NUEVO BLOQUE ---
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(db.foodItemDao(), prefsRepository) as T
            }

            // --- AÑADIR ESTE NUEVO BLOQUE ---
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(prefsRepository) as T
            }
            // --- FIN DEL BLOQUE ---
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}