// en app/src/main/java/bw/development/nutriscan/ui/viewmodels/SettingsViewModel.kt
package bw.development.nutriscan.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bw.development.nutriscan.data.UserPreferences
import bw.development.nutriscan.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de Ajustes
data class SettingsUiState(
    val prefs: UserPreferences = UserPreferences(),
    val isSaved: Boolean = false
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Carga las preferencias guardadas tan pronto como se inicie el ViewModel
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val currentPrefs = userPreferencesRepository.userPreferencesFlow.first()
            _uiState.update { it.copy(prefs = currentPrefs) }
        }
    }

    // Funciones para actualizar el estado cuando el usuario cambia un valor
    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(gender = gender), isSaved = false) }
    }

    fun onAgeChange(age: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(age = age.toIntOrNull() ?: 0), isSaved = false) }
    }

    fun onHeightChange(height: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(height = height.toDoubleOrNull() ?: 0.0), isSaved = false) }
    }

    fun onWeightChange(weight: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(weight = weight.toDoubleOrNull() ?: 0.0), isSaved = false) }
    }

    fun onActivityLevelChange(level: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(activityLevel = level), isSaved = false) }
    }

    fun onGoalChange(goal: String) {
        _uiState.update { it.copy(prefs = it.prefs.copy(goal = goal), isSaved = false) }
    }

    // Función para guardar los cambios en DataStore
    fun savePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.saveProfileData(_uiState.value.prefs)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding()
        }
    }
}