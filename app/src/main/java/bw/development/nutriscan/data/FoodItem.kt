package bw.development.nutriscan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Double,
    val category: String, // "Desayuno", "Comida", "Cena"
    val calories: Int,
    val protein: Double?,
    val fat: Double?,
    val carbs: Double?,
    val timestamp: Long,

    val imageUri: String? = null // Guardará la ruta a la imagen
)

