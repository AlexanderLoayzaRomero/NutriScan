// en /app/src/main/java/bw/development/nutriscan/data/FoodItemDao.kt
package bw.development.nutriscan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insertFoodItem(foodItem: FoodItem)

    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItem)

    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getFoodItemById(id: Int): FoodItem?

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY id DESC")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items ORDER BY timestamp DESC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE category = :category AND timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getFoodItemsByCategoryForDay(category: String, startOfDay: Long, endOfDay: Long): Flow<List<FoodItem>>

    // --- AÑADIR ESTA NUEVA FUNCIÓN ---
    @Query("SELECT * FROM food_items WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getAllFoodItemsForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodItem>>
}