// en /app/src/main/java/bw/development/nutriscan/ui/viewmodels/FoodLogViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
// AÑADIR ESTE IMPORT
import bw.development.nutriscan.ui.screens.LogItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
// AÑADIR ESTE IMPORT
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
// AÑADIR ESTOS IMPORTS
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FoodLogViewModel(foodItemDao: FoodItemDao) : ViewModel() {

    // CAMBIO: Renombramos 'allFoodItems' a 'groupedFoodItems'
    // y cambiamos su tipo a List<LogItem>
    val groupedFoodItems: StateFlow<List<LogItem>> =
        foodItemDao.getAllFoodItems() // 1. Obtenemos la lista plana de la BD
            .map { groupFoodItemsByDate(it) } // 2. La transformamos con nuestra nueva lógica
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    // --- NUEVA FUNCIÓN ---
    // Esta es la función principal que hace la agrupación
    private fun groupFoodItemsByDate(items: List<FoodItem>): List<LogItem> {
        val groupedItems = mutableListOf<LogItem>()
        var lastHeaderCalendar: Calendar? = null

        // Obtenemos calendarios de referencia
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        items.forEach { item ->
            val itemCalendar = Calendar.getInstance().apply { timeInMillis = item.timestamp }

            // Comprueba si este item es de un día diferente al anterior
            if (!isSameDay(itemCalendar, lastHeaderCalendar)) {
                // Si es un nuevo día, obtenemos el título (Hoy, Ayer, etc.)
                val title = getDateHeader(itemCalendar, today, yesterday)
                // Añadimos el encabezado a nuestra lista
                groupedItems.add(LogItem.Header(title))
                // Actualizamos la referencia del "último día"
                lastHeaderCalendar = itemCalendar
            }
            // Añadimos el alimento (que pertenece al encabezado que acabamos de poner)
            groupedItems.add(LogItem.Food(item))
        }
        return groupedItems
    }

    // --- NUEVA FUNCIÓN DE AYUDA ---
    // Devuelve el String "Hoy", "Ayer" o la fecha formateada
    private fun getDateHeader(itemCalendar: Calendar, today: Calendar, yesterday: Calendar): String {
        return when {
            isSameDay(itemCalendar, today) -> "Hoy"
            isSameDay(itemCalendar, yesterday) -> "Ayer"
            else -> {
                // Locale("es", "ES") es para que ponga "de" en minúscula y en español
                val formatter = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
                formatter.format(itemCalendar.time)
            }
        }
    }

    // --- NUEVA FUNCIÓN DE AYUDA ---
    // Compara si dos calendarios son el mismo día del mismo año
    private fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        if (cal1 == null || cal2 == null) {
            return false
        }
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}