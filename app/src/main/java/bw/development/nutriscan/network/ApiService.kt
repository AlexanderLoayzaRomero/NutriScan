package bw.development.nutriscan.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    // Llama a la API de OpenFoodFacts para un producto específico por código de barras
    @GET("api/v0/product/{barcode}.json?fields=product_name,nutriments")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): FoodApiResponse
}