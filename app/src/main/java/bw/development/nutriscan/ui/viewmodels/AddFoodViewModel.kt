package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import bw.development.nutriscan.network.Product
import bw.development.nutriscan.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt

data class AddFoodUiState(
    val foodName: String = "",
    val quantity: String = "100",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val category: String = "Desayuno",
    val isLoading: Boolean = false,

    // Campos base para auto-cálculo
    val baseCalories100g: Double? = null,
    val baseProtein100g: Double? = null,
    val baseFat100g: Double? = null,
    val baseCarbs100g: Double? = null,

    // NUEVO: Lista para los resultados de búsqueda
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false // Spinner de búsqueda
)

class AddFoodViewModel(private val foodItemDao: FoodItemDao) : ViewModel() {

    private val apiService = RetrofitInstance.api
    private val decimalFormat = DecimalFormat("#.#")
    private var searchJob: Job? = null // Job para el debouncing

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    // --- Funciones para manejar eventos de la UI ---

    // MODIFICADO: Ahora también dispara la búsqueda
    fun onFoodNameChanged(name: String) {
        _uiState.update { it.copy(foodName = name) }

        // Lógica de Debouncing
        searchJob?.cancel() // Cancela la búsqueda anterior

        // No buscar si el texto es muy corto
        if (name.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        // Inicia un nuevo job de búsqueda con retraso
        searchJob = viewModelScope.launch {
            delay(350L) // Espera 350ms después de la última pulsación
            searchFoodByNameInternal(name)
        }
    }

    fun onQuantityChanged(newQtyStr: String) {
        val currentState = _uiState.value
        val newQtyDouble = newQtyStr.toDoubleOrNull()

        if (newQtyDouble == null || newQtyDouble <= 0) {
            _uiState.update {
                it.copy(
                    quantity = newQtyStr,
                    calories = "",
                    protein = "",
                    fat = "",
                    carbs = ""
                )
            }
        } else {
            val factor = newQtyDouble / 100.0
            _uiState.update {
                it.copy(
                    quantity = newQtyStr,
                    calories = currentState.baseCalories100g?.let { base ->
                        (base * factor).roundToInt().toString()
                    } ?: it.calories,
                    protein = currentState.baseProtein100g?.let { base ->
                        decimalFormat.format(base * factor)
                    } ?: it.protein,
                    fat = currentState.baseFat100g?.let { base ->
                        decimalFormat.format(base * factor)
                    } ?: it.fat,
                    carbs = currentState.baseCarbs100g?.let { base ->
                        decimalFormat.format(base * factor)
                    } ?: it.carbs
                )
            }
        }
    }

    fun onCaloriesChanged(cal: String) {
        _uiState.update {
            it.copy(
                calories = cal,
                baseCalories100g = null // Rompe el enlace
            )
        }
    }

    fun onProteinChanged(p: String) {
        _uiState.update {
            it.copy(
                protein = p,
                baseProtein100g = null // Rompe el enlace
            )
        }
    }

    fun onFatChanged(f: String) {
        _uiState.update {
            it.copy(
                fat = f,
                baseFat100g = null // Rompe el enlace
            )
        }
    }

    fun onCarbsChanged(c: String) {
        _uiState.update {
            it.copy(
                carbs = c,
                baseCarbs100g = null // Rompe el enlace
            )
        }
    }

    fun onCategoryChanged(cat: String) {
        _uiState.update { it.copy(category = cat) }
    }

    // --- Lógica de API ---

    // Función interna para la búsqueda (llamada por el debouncer)
    private fun searchFoodByNameInternal(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val response = apiService.searchFoodByName(query)
                _uiState.update {
                    it.copy(
                        // Filtramos para mostrar solo productos con datos nutricionales
                        searchResults = response.products.filter { product ->
                            product.productName != null && product.nutriments != null
                        },
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                // Manejar error de red
                _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
            }
        }
    }

    // NUEVO: Llamado cuando el usuario selecciona un item de la lista
    fun onSearchResultSelected(product: Product) {
        searchJob?.cancel() // Detener cualquier búsqueda pendiente

        val nutriments = product.nutriments
        val calories100g = nutriments?.energyKcal100g
        val protein100g = nutriments?.proteins100g
        val fat100g = nutriments?.fat100g
        val carbs100g = nutriments?.carbohydrates100g

        _uiState.update {
            it.copy(
                foodName = product.productName ?: "",
                quantity = "100", // Resetea a 100g

                // Rellena los valores mostrados
                calories = calories100g?.roundToInt()?.toString() ?: "",
                protein = protein100g?.let { p -> decimalFormat.format(p) } ?: "",
                fat = fat100g?.let { f -> decimalFormat.format(f) } ?: "",
                carbs = carbs100g?.let { c -> decimalFormat.format(c) } ?: "",

                // Guarda los valores base para el auto-cálculo
                baseCalories100g = calories100g,
                baseProtein100g = protein100g,
                baseFat100g = fat100g,
                baseCarbs100g = carbs100g,

                searchResults = emptyList(), // Cierra/oculta la lista de resultados
                isSearching = false
            )
        }
    }

    // NUEVO: Para cerrar el menú desplegable si el usuario pincha fuera
    fun clearSearchResults() {
        _uiState.update { it.copy(searchResults = emptyList()) }
    }

    // Función de escáner de código de barras
    fun fetchFoodDetails(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Spinner grande
            try {
                val response = apiService.getProductByBarcode(barcode)
                if (response.status == 1 && response.product != null) {
                    // Reutilizamos la lógica de selección de producto
                    onSearchResultSelected(response.product)
                } else {
                    // TODO: Manejar "producto no encontrado" (ej. con un Toast)
                }
            } catch (e: Exception) {
                // TODO: Manejar error de red
            } finally {
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
                protein = currentState.protein.replace(",", ".").toDoubleOrNull(),
                fat = currentState.fat.replace(",", ".").toDoubleOrNull(),
                carbs = currentState.carbs.replace(",", ".").toDoubleOrNull()
            )
            viewModelScope.launch {
                foodItemDao.insertFoodItem(newFoodItem)
                onSaveSuccess()
            }
        } else {
            // TODO: Mostrar error de validación
        }
    }
}