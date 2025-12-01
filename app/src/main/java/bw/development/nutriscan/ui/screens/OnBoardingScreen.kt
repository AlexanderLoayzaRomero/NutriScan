package bw.development.nutriscan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.data.UserProfile
import bw.development.nutriscan.ui.viewmodels.SettingsViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext)
    )
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.prefs
    val scope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "¡Bienvenido a NutriScan!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Configuremos tu perfil para calcular tus metas calóricas exactas.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = if (prefs.age == 0) "" else prefs.age.toString(),
                onValueChange = { viewModel.onAgeChange(it) },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (prefs.height == 0.0) "" else prefs.height.toString(),
                    onValueChange = { viewModel.onHeightChange(it) },
                    label = { Text("Altura") },
                    suffix = { Text("cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (prefs.weight == 0.0) "" else prefs.weight.toString(),
                    onValueChange = { viewModel.onWeightChange(it) },
                    label = { Text("Peso") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            DropdownInput("Género", prefs.gender, mapOf(
                UserProfile.GENDER_MALE to "Hombre",
                UserProfile.GENDER_FEMALE to "Mujer"
            )) { viewModel.onGenderChange(it) }

            Spacer(modifier = Modifier.height(16.dp))

            DropdownInput("Actividad Física", prefs.activityLevel, mapOf(
                UserProfile.ACTIVITY_SEDENTARY to "Sedentario",
                UserProfile.ACTIVITY_LIGHT to "Ligero",
                UserProfile.ACTIVITY_MODERATE to "Moderado",
                UserProfile.ACTIVITY_ACTIVE to "Activo"
            )) { viewModel.onActivityLevelChange(it) }

            Spacer(modifier = Modifier.height(16.dp))

            DropdownInput("Objetivo", prefs.goal, mapOf(
                UserProfile.GOAL_LOSE to "Bajar peso",
                UserProfile.GOAL_MAINTAIN to "Mantener",
                UserProfile.GOAL_GAIN to "Subir peso"
            )) { viewModel.onGoalChange(it) }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.savePreferences()
                        viewModel.completeOnboarding()
                        onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = prefs.age > 0 && prefs.weight > 0.0 && prefs.height > 0.0
            ) {
                Text("Comenzar a Escanear")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInput(
    label: String,
    selectedKey: String,
    options: Map<String, String>,
    onSelection: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = options[selectedKey] ?: "Seleccionar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, labelText) ->
                DropdownMenuItem(
                    text = { Text(labelText) },
                    onClick = {
                        onSelection(key)
                        expanded = false
                    }
                )
            }
        }
    }
}