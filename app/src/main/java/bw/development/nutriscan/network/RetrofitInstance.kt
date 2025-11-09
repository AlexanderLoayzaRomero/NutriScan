package bw.development.nutriscan.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType // <-- AÑADIR ESTA LÍNEA
import retrofit2.Retrofit

object RetrofitInstance {

    private const val BASE_URL = "https://world.openfoodfacts.org/"

    // Configuramos Json para que ignore campos que no mapeamos en nuestras data classes
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                // "application/json".toMediaType() ahora funcionará gracias al import
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}