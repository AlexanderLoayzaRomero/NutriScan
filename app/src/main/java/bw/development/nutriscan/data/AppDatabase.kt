// en /app/src/main/java/bw/development/nutriscan/data/AppDatabase.kt
package bw.development.nutriscan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// --- CAMBIA version = 1 POR version = 2 ---
@Database(entities = [FoodItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriscan_database"
                )
                    // --- AÑADIR ESTA LÍNEA ---
                    // Esto borrará la base de datos y la creará de nuevo.
                    // Es seguro en desarrollo, pero no en producción.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}