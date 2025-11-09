package bw.development.nutriscan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Asegúrate de que FoodItem::class está definido y es una @Entity
@Database(entities = [FoodItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // ESTA ES LA SINTAXIS CORRECTA DE TRES ARGUMENTOS
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriscan_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
