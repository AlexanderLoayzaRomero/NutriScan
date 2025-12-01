// en app/src/main/java/bw/development/nutriscan/ui/screens/SettingsScreen.kt
package bw.development.nutriscan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.data.UserProfile
import bw.development.nutriscan.ui.viewmodels.SettingsViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.prefs
    val context = LocalContext.current

    // Mostrar un Toast cuando se guarda
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Perfil guardado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil y Metas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Campos de texto ---
            OutlinedTextField(
                value = if (prefs.age == 0) "" else prefs.age.toString(),
                onValueChange = { viewModel.onAgeChange(it) },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

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

            // --- Menús desplegables ---
            DropdownSelector(
                label = "Género",
                options = mapOf(
                    UserProfile.GENDER_MALE to "Hombre",
                    UserProfile.GENDER_FEMALE to "Mujer"
                ),
                selectedKey = prefs.gender,
                onKeySelected = { viewModel.onGenderChange(it) }
            )

            DropdownSelector(
                label = "Nivel de Actividad",
                options = mapOf(
                    UserProfile.ACTIVITY_SEDENTARY to "Sedentario (nada)",
                    UserProfile.ACTIVITY_LIGHT to "Ligero (1-3 días)",
                    UserProfile.ACTIVITY_MODERATE to "Moderado (3-5 días)",
                    UserProfile.ACTIVITY_ACTIVE to "Activo (6-7 días)"
                ),
                selectedKey = prefs.activityLevel,
                onKeySelected = { viewModel.onActivityLevelChange(it) }
            )

            DropdownSelector(
                label = "Mi Objetivo",
                options = mapOf(
                    UserProfile.GOAL_LOSE to "Bajar peso",
                    UserProfile.GOAL_MAINTAIN to "Mantener peso",
                    UserProfile.GOAL_GAIN to "Subir peso"
                ),
                selectedKey = prefs.goal,
                onKeySelected = { viewModel.onGoalChange(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Botón de Guardar ---
            Button(
                onClick = { viewModel.savePreferences() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Perfil")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider() // Una línea finita para separar
            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Cerrar Sesión (Rojo)
            Button(
                onClick = {
                    // 1. Cerrar en Firebase
                    Firebase.auth.signOut()

                    // 2. Cerrar en Google (Para que pida cuenta la próxima vez)
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)

                    googleSignInClient.signOut().addOnCompleteListener {
                        // 3. Navegar fuera (al Login)
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error, // Rojo
                    contentColor = MaterialTheme.colorScheme.onError  // Texto blanco/claro
                )
            ) {
                Text("Cerrar Sesión")
            }

            Spacer(modifier = Modifier.height(32.dp)) // Espacio final para que se vea bien
        }
    }
}

// --- Composable de Ayuda para los Dropdowns ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: Map<String, String>, // <Key, DisplayName>
    selectedKey: String,
    onKeySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDisplayName = options[selectedKey] ?: "Seleccionar"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDisplayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (key, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onKeySelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}