package bw.development.nutriscan.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Llama a la API de OpenFoodFacts para un producto específico por código de barras
    @GET("api/v0/product/{barcode}.json?fields=product_name,nutriments")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): FoodApiResponse

    // MODIFICADO: Añadimos el parámetro de ordenación
    @GET("cgi/search.pl?json=1&search_simple=1&action=process&page_size=10&fields=product_name,nutriments&sort_by=nutriscore_score")
    suspend fun searchFoodByName(
        @Query("search_terms") query: String
    ): SearchApiResponse
}