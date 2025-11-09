package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import bw.development.nutriscan.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Data class para mantener el estado de la UI
data class AddFoodUiState(
    val foodName: String = "",
    val quantity: String = "100", // Usamos 100g como base
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val category: String = "Desayuno",
    val isLoading: Boolean = false
)

class AddFoodViewModel(private val foodItemDao: FoodItemDao) : ViewModel() {

    // Instancia de la API (simplificado, sin DI)
    private val apiService = RetrofitInstance.api

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    // --- Funciones para manejar eventos de la UI ---

    fun onFoodNameChanged(name: String) {
        _uiState.update { it.copy(foodName = name) }
    }
    fun onQuantityChanged(qty: String) {
        _uiState.update { it.copy(quantity = qty) }
    }
    fun onCaloriesChanged(cal: String) {
        _uiState.update { it.copy(calories = cal) }
    }
    fun onProteinChanged(p: String) {
        _uiState.update { it.copy(protein = p) }
    }
    fun onFatChanged(f: String) {
        _uiState.update { it.copy(fat = f) }
    }
    fun onCarbsChanged(c: String) {
        _uiState.update { it.copy(carbs = c) }
    }
    fun onCategoryChanged(cat: String) {
        _uiState.update { it.copy(category = cat) }
    }

    // --- Lógica de API ---

    fun fetchFoodDetails(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getProductByBarcode(barcode)
                if (response.status == 1 && response.product != null) {
                    val product = response.product
                    val nutriments = product.nutriments

                    // Actualizamos el estado de la UI con los datos de la API
                    _uiState.update {
                        it.copy(
                            foodName = product.productName ?: "",
                            // Los valores vienen por 100g, la cantidad por defecto es 100
                            calories = nutriments?.energyKcal100g?.roundToInt()?.toString() ?: "",
                            protein = nutriments?.proteins100g?.toString() ?: "",
                            fat = nutriments?.fat100g?.toString() ?: "",
                            carbs = nutriments?.carbohydrates100g?.toString() ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    // TODO: Manejar "producto no encontrado" (ej. con un Toast)
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                // TODO: Manejar error de red
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    // --- Lógica de Base de Datos ---

    fun saveFoodItem(onSaveSuccess: () -> Unit) {
        val currentState = _uiState.value
        val qty = currentState.quantity.toDoubleOrNull() ?: 0.0
        val cal = currentState.calories.toIntOrNull() ?: 0

        if (currentState.foodName.isNotBlank() && qty > 0 && cal > 0) {
            val newFoodItem = FoodItem(
                name = currentState.foodName,
                quantity = qty,
                category = currentState.category,
                calories = cal,
                protein = currentState.protein.toDoubleOrNull(),
                fat = currentState.fat.toDoubleOrNull(),
                carbs = currentState.carbs.toDoubleOrNull()
            )
            viewModelScope.launch {
                foodItemDao.insertFoodItem(newFoodItem)
                onSaveSuccess() // Llama al callback para navegar hacia atrás
            }
        } else {
            // TODO: Mostrar error de validación (ej. con un Toast)
        }
    }
}