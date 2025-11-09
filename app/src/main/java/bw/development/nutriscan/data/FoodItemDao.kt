package bw.development.nutriscan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insertFoodItem(foodItem: FoodItem)

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY id DESC")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items ORDER BY timestamp DESC") // Ordena por el timestamp
    fun getAllFoodItems(): Flow<List<FoodItem>>

}