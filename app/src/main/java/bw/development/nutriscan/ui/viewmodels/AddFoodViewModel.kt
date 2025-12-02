package bw.development.nutriscan.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.FoodItem
import bw.development.nutriscan.data.FoodItemDao
import bw.development.nutriscan.network.Product
import bw.development.nutriscan.network.RetrofitInstance
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.DecimalFormat
import kotlin.math.roundToInt
import bw.development.nutriscan.BuildConfig

data class AddFoodUiState(
    val editingItemId: Int? = null,
    val originalTimestamp: Long? = null,
    val imageUri: String? = null,

    val foodName: String = "",
    val quantity: String = "100",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val category: String = "Desayuno",
    val isLoading: Boolean = false,

    val baseCalories100g: Double? = null,
    val baseProtein100g: Double? = null,
    val baseFat100g: Double? = null,
    val baseCarbs100g: Double? = null, // <-- Este es el nombre correcto

    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val userMessage: String? = null
)

@Serializable
private data class AIAnalysisResponse(
    val foodName: String,
    val calories100g: Int,
    val protein100g: Double,
    val fat100g: Double,
    val carbs100g: Double
)

class AddFoodViewModel(private val foodItemDao: FoodItemDao) : ViewModel() {

    private val apiService = RetrofitInstance.api
    private val decimalFormat = DecimalFormat("#.#")
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    // ... (onFoodNameChanged, onQuantityChanged, etc. HASTA onSearchResultSelected) ...
    fun onFoodNameChanged(name: String) {
        _uiState.update { it.copy(foodName = name) }
        searchJob?.cancel()
        if (name.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350L)
            searchFoodByNameInternal(name)
        }
    }
    fun onQuantityChanged(newQtyStr: String) {
        val currentState = _uiState.value
        val newQtyDouble = newQtyStr.toDoubleOrNull()
        if (newQtyDouble == null || newQtyDouble <= 0) {
            _uiState.update {
                it.copy(quantity = newQtyStr, calories = "", protein = "", fat = "", carbs = "")
            }
        } else {
            val factor = newQtyDouble / 100.0
            _uiState.update {
                it.copy(
                    quantity = newQtyStr,
                    calories = currentState.baseCalories100g?.let { base -> (base * factor).roundToInt().toString() } ?: it.calories,
                    protein = currentState.baseProtein100g?.let { base -> decimalFormat.format(base * factor) } ?: it.protein,
                    fat = currentState.baseFat100g?.let { base -> decimalFormat.format(base * factor) } ?: it.fat,
                    carbs = currentState.baseCarbs100g?.let { base -> decimalFormat.format(base * factor) } ?: it.carbs
                )
            }
        }
    }
    fun onCaloriesChanged(cal: String) { _uiState.update { it.copy(calories = cal, baseCalories100g = null) } }
    fun onProteinChanged(p: String) { _uiState.update { it.copy(protein = p, baseProtein100g = null) } }
    fun onFatChanged(f: String) { _uiState.update { it.copy(fat = f, baseFat100g = null) } }
    fun onCarbsChanged(c: String) { _uiState.update { it.copy(carbs = c, baseCarbs100g = null) } }
    fun onCategoryChanged(cat: String) { _uiState.update { it.copy(category = cat) } }
    private fun searchFoodByNameInternal(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val response = apiService.searchFoodByName(query)
                _uiState.update {
                    it.copy(
                        searchResults = response.products.filter { product -> product.productName != null && product.nutriments != null },
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, searchResults = emptyList(), userMessage = "Error de red: ${e.message}") }
            }
        }
    }

    // --- FUNCIÓN onSearchResultSelected (CORREGIDA) ---
    fun onSearchResultSelected(product: Product) {
        searchJob?.cancel()
        val nutriments = product.nutriments
        val calories100g = nutriments?.energyKcal100g
        val protein100g = nutriments?.proteins100g
        val fat100g = nutriments?.fat100g
        val carbs100g = nutriments?.carbohydrates100g
        _uiState.update {
            it.copy(
                foodName = product.productName ?: "",
                quantity = "100",
                calories = calories100g?.roundToInt()?.toString() ?: "",
                protein = protein100g?.let { p -> decimalFormat.format(p) } ?: "",
                fat = fat100g?.let { f -> decimalFormat.format(f) } ?: "",
                carbs = carbs100g?.let { c -> decimalFormat.format(c) } ?: "",
                baseCalories100g = calories100g,
                baseProtein100g = protein100g,
                baseFat100g = fat100g,
                baseCarbs100g = carbs100g, // <-- ¡AQUÍ ESTÁ LA CORRECCIÓN!
                searchResults = emptyList(),
                isSearching = false,
                imageUri = null
            )
        }
    }
    fun clearSearchResults() { _uiState.update { it.copy(searchResults = emptyList()) } }
    fun fetchFoodDetails(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getProductByBarcode(barcode)
                if (response.status == 1 && response.product != null) {
                    onSearchResultSelected(response.product)
                } else {
                    _uiState.update { it.copy(userMessage = "Producto no encontrado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Error de red: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    fun userMessageShown() { _uiState.update { it.copy(userMessage = null) } }

    fun onImagePicked(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun loadFoodItem(id: Int) {
        if (id == 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val foodItem = foodItemDao.getFoodItemById(id)
            if (foodItem != null) {
                _uiState.update {
                    it.copy(
                        editingItemId = foodItem.id,
                        originalTimestamp = foodItem.timestamp,
                        imageUri = foodItem.imageUri,
                        foodName = foodItem.name,
                        quantity = decimalFormat.format(foodItem.quantity),
                        calories = foodItem.calories.toString(),
                        protein = foodItem.protein?.let { p -> decimalFormat.format(p) } ?: "",
                        fat = foodItem.fat?.let { f -> decimalFormat.format(f) } ?: "",
                        carbs = foodItem.carbs?.let { c -> decimalFormat.format(c) } ?: "",
                        category = foodItem.category,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, userMessage = "No se encontró el alimento") }
            }
        }
    }

    fun saveFoodItem(onSaveSuccess: () -> Unit) {
        val currentState = _uiState.value
        val qty = currentState.quantity.toDoubleOrNull() ?: 0.0
        val cal = currentState.calories.toIntOrNull() ?: 0

        if (currentState.foodName.isNotBlank() && qty > 0 && cal > 0) {
            val foodItem = FoodItem(
                id = currentState.editingItemId ?: 0,
                name = currentState.foodName,
                quantity = qty,
                category = currentState.category,
                calories = cal,
                protein = currentState.protein.replace(",", ".").toDoubleOrNull(),
                fat = currentState.fat.replace(",", ".").toDoubleOrNull(),
                carbs = currentState.carbs.replace(",", ".").toDoubleOrNull(),
                timestamp = currentState.originalTimestamp ?: System.currentTimeMillis(),
                imageUri = currentState.imageUri
            )

            viewModelScope.launch {
                if (currentState.editingItemId != null) {
                    foodItemDao.updateFoodItem(foodItem)
                } else {
                    foodItemDao.insertFoodItem(foodItem)
                }
                onSaveSuccess()
            }
        } else {
            _uiState.update { it.copy(userMessage = "Por favor, rellene Nombre, Cantidad y Calorías.") }
        }
    }

    // Configuración de Gemini (Modelo Flash para rapidez)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    fun analyzeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = "Analizando alimentos con IA...") }

            try {
                // 1. Convertir URI a Bitmap
                val bitmap = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                }

                if (bitmap != null) {
                    // 2. Prompt para la IA
                    val prompt = """
                        Analiza esta imagen de comida. Identifica el alimento principal.
                        Devuelve SOLAMENTE un objeto JSON con esta estructura exacta (sin md ni backticks):
                        {
                            "foodName": "Nombre del plato en español",
                            "calories100g": valor entero aproximado de calorias por 100g,
                            "protein100g": valor decimal de proteinas por 100g,
                            "fat100g": valor decimal de grasas por 100g,
                            "carbs100g": valor decimal de carbohidratos por 100g
                        }
                        Si no es comida, pon valores en 0 y nombre "Desconocido".
                    """.trimIndent()

                    // 3. Llamada a Gemini
                    val response = withContext(Dispatchers.IO) {
                        generativeModel.generateContent(
                            content {
                                image(bitmap)
                                text(prompt)
                            }
                        )
                    }

                    // 4. Procesar respuesta
                    response.text?.let { jsonString ->
                        val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                        val result = Json { ignoreUnknownKeys = true }.decodeFromString<AIAnalysisResponse>(cleanJson)

                        // 5. Actualizar la UI
                        _uiState.update {
                            it.copy(
                                foodName = result.foodName,
                                quantity = "100", // Asumimos 100g por defecto
                                calories = result.calories100g.toString(),
                                protein = decimalFormat.format(result.protein100g),
                                fat = decimalFormat.format(result.fat100g),
                                carbs = decimalFormat.format(result.carbs100g),
                                // Guardamos los base para recalcular si cambian la cantidad
                                baseCalories100g = result.calories100g.toDouble(),
                                baseProtein100g = result.protein100g,
                                baseFat100g = result.fat100g,
                                baseCarbs100g = result.carbs100g,
                                isLoading = false,
                                userMessage = "¡Alimento identificado!"
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, userMessage = "Error al cargar la imagen") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, userMessage = "Error de IA: ${e.message}") }
            }
        }
    }
}