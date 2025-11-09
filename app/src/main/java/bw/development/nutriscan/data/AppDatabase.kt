// en /app/src/main/java/bw/development/nutriscan/data/AppDatabase.kt
package bw.development.nutriscan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// --- CAMBIA version = 1 POR version = 2 ---
@Database(entities = [FoodItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // --- NUEVA MIGRACIÓN de 1 a 2 (La que hicimos antes) ---
        // (La dejamos aquí para futuros usuarios que instalen desde v1)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items ADD COLUMN timestamp LONG NOT NULL DEFAULT 0")
            }
        }

        // --- NUEVA MIGRACIÓN de 2 a 3 (Para la imagen) ---
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items ADD COLUMN imageUri TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriscan_database"
                )
                    // CAMBIO: Quitamos fallback y añadimos las migraciones
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}