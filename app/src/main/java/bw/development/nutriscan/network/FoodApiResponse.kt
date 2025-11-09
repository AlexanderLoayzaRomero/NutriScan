package bw.development.nutriscan.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelo para la respuesta completa de la API
@Serializable
data class FoodApiResponse(
    @SerialName("status")
    val status: Int,
    @SerialName("product")
    val product: Product?
)

// Modelo para el objeto "product"
@Serializable
data class Product(
    @SerialName("product_name")
    val productName: String?,
    @SerialName("nutriments")
    val nutriments: Nutriments?
)

// Modelo para los nutrientes
@Serializable
data class Nutriments(
    // Valores por 100g
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Double?,
    @SerialName("proteins_100g")
    val proteins100g: Double?,
    @SerialName("fat_100g")
    val fat100g: Double?,
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Double?
)