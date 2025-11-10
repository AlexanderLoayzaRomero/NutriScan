// en app/src/main/java/bw/development/nutriscan/data/UserPreferencesRepository.kt
package bw.development.nutriscan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

// --- 1. Definición de constantes para las opciones ---
object UserProfile {
    // Opciones de Género
    const val GENDER_MALE = "male"
    const val GENDER_FEMALE = "female"

    // Opciones de Nivel de Actividad
    const val ACTIVITY_SEDENTARY = "sedentary" // Poco o nada de ejercicio
    const val ACTIVITY_LIGHT = "light"       // Ejercicio ligero 1-3 días/sem
    const val ACTIVITY_MODERATE = "moderate"   // Ejercicio moderado 3-5 días/sem
    const val ACTIVITY_ACTIVE = "active"     // Ejercicio intenso 6-7 días/sem

    // Opciones de Objetivo
    const val GOAL_LOSE = "lose"   // Bajar peso (-500 kcal)
    const val GOAL_MAINTAIN = "maintain" // Mantener peso
    const val GOAL_GAIN = "gain"   // Subir peso (+500 kcal)
}

// --- 2. Data class para guardar los datos del perfil ---
data class UserPreferences(
    val gender: String = UserProfile.GENDER_MALE,
    val age: Int = 30,
    val height: Double = 170.0,
    val weight: Double = 70.0,
    val activityLevel: String = UserProfile.ACTIVITY_LIGHT,
    val goal: String = UserProfile.GOAL_MAINTAIN
)

// --- 3. El Repositorio que maneja DataStore ---

// Inicializa DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    // --- 4. Definición de las "llaves" para guardar en DataStore ---
    companion object {
        val KEY_GENDER = stringPreferencesKey("gender")
        val KEY_AGE = intPreferencesKey("age")
        val KEY_HEIGHT = doublePreferencesKey("height")
        val KEY_WEIGHT = doublePreferencesKey("weight")
        val KEY_ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val KEY_GOAL = stringPreferencesKey("goal")

        private const val DEFAULT_GOAL = 2000 // Meta por defecto si no hay datos
    }

    // --- 5. Flow para LEER todas las preferencias ---
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                gender = preferences[KEY_GENDER] ?: UserProfile.GENDER_MALE,
                age = preferences[KEY_AGE] ?: 30,
                height = preferences[KEY_HEIGHT] ?: 170.0,
                weight = preferences[KEY_WEIGHT] ?: 70.0,
                activityLevel = preferences[KEY_ACTIVITY_LEVEL] ?: UserProfile.ACTIVITY_LIGHT,
                goal = preferences[KEY_GOAL] ?: UserProfile.GOAL_MAINTAIN
            )
        }

    // --- 6. Flow para OBTENER la meta calórica CALCULADA ---
    val calorieGoalFlow: Flow<Int> = userPreferencesFlow
        .map { prefs ->
            calculateCalorieGoal(prefs)
        }

    // --- 7. Funciones para GUARDAR las preferencias ---
    suspend fun saveProfileData(prefs: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[KEY_GENDER] = prefs.gender
            preferences[KEY_AGE] = prefs.age
            preferences[KEY_HEIGHT] = prefs.height
            preferences[KEY_WEIGHT] = prefs.weight
            preferences[KEY_ACTIVITY_LEVEL] = prefs.activityLevel
            preferences[KEY_GOAL] = prefs.goal
        }
    }

    // --- 8. Lógica de CÁLCULO de calorías ---
    private fun calculateCalorieGoal(prefs: UserPreferences): Int {
        // Si faltan datos clave, devuelve el default
        if (prefs.age <= 0 || prefs.height <= 0 || prefs.weight <= 0) {
            return DEFAULT_GOAL
        }

        // --- A. Cálculo de Metabolismo Basal (BMR) con Mifflin-St Jeor ---
        val bmr = if (prefs.gender == UserProfile.GENDER_MALE) {
            (10 * prefs.weight) + (6.25 * prefs.height) - (5 * prefs.age) + 5
        } else { // GENDER_FEMALE
            (10 * prefs.weight) + (6.25 * prefs.height) - (5 * prefs.age) - 161
        }

        // --- B. Cálculo de Gasto Calórico Total (TDEE) ---
        val activityMultiplier = when (prefs.activityLevel) {
            UserProfile.ACTIVITY_SEDENTARY -> 1.2
            UserProfile.ACTIVITY_LIGHT -> 1.375
            UserProfile.ACTIVITY_MODERATE -> 1.55
            UserProfile.ACTIVITY_ACTIVE -> 1.725
            else -> 1.375
        }
        val tdee = bmr * activityMultiplier

        // --- C. Ajuste final según el Objetivo ---
        val finalGoal = when (prefs.goal) {
            UserProfile.GOAL_LOSE -> tdee - 500 // Déficit de 500 kcal
            UserProfile.GOAL_MAINTAIN -> tdee
            UserProfile.GOAL_GAIN -> tdee + 500 // Superávit de 500 kcal
            else -> tdee
        }

        return finalGoal.roundToInt()
    }
}