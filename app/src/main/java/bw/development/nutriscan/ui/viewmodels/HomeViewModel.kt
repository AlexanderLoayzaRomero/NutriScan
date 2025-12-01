// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/HomeViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.NutriWidget
import bw.development.nutriscan.data.FoodItemDao
// --- AÑADIR IMPORT ---
import bw.development.nutriscan.data.UserPreferencesRepository
import bw.development.nutriscan.updateAppWidget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
// --- AÑADIR IMPORT ---
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlin.jvm.java

data class HomeUiState(
    val totalCalories: Int = 0,
    val breakfastCalories: Int = 0,
    val lunchCalories: Int = 0,
    val dinnerCalories: Int = 0,
    val snackCalories: Int = 0,
    val calorieGoal: Int = 0,
    // --- NUEVOS CAMPOS ---
    val totalProtein: Int = 0,
    val totalFat: Int = 0,
    val totalCarbs: Int = 0
)

// --- CAMBIO: El constructor ahora pide el UserPreferencesRepository ---
class HomeViewModel(
    foodItemDao: FoodItemDao,
    userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
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

                var p = 0.0
                var f = 0.0
                var c = 0.0

                items.forEach {
                    total += it.calories

                    p += it.protein ?: 0.0
                    f += it.fat ?: 0.0
                    c += it.carbs ?: 0.0

                    when (it.category) {
                        "Desayuno" -> breakfast += it.calories
                        "Comida" -> lunch += it.calories
                        "Cena" -> dinner += it.calories
                        "Snack" -> snack += it.calories
                    }
                }

                val sharedPrefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putInt("total_calories", total)
                    putInt("goal_calories", goal)
                    putInt("widget_protein", p.toInt())
                    putInt("widget_fat", f.toInt())
                    putInt("widget_carbs", c.toInt())
                    apply()
                }

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        NutriWidget::class.java
                    )
                )
                ids.forEach { id ->
                    updateAppWidget(context, appWidgetManager, id)
                }

                HomeUiState(
                    totalCalories = total,
                    breakfastCalories = breakfast,
                    lunchCalories = lunch,
                    dinnerCalories = dinner,
                    snackCalories = snack,
                    calorieGoal = goal,
                    totalProtein = p.toInt(),
                    totalFat = f.toInt(),
                    totalCarbs = c.toInt()
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = HomeUiState()
            )
}