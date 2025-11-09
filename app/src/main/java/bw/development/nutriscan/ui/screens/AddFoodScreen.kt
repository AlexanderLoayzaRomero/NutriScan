package bw.development.nutriscan.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.ui.viewmodels.AddFoodViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddFoodViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )
) {
    // Recolectamos el estado desde el ViewModel
    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf("Desayuno", "Comida", "Cena", "Snack")
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // --- Lógica del Escáner ---
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val scanner = GmsBarcodeScanning.getClient(context, options)

    // Launcher para el escáner
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            // Este callback se ha movido al onSuccessListener de startScan
        }
    )

    // Función para iniciar el escaneo
    val startScan = {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // Éxito: tenemos el código de barras
                val rawValue = barcode.rawValue
                if (rawValue != null) {
                    // Llamamos al ViewModel con el código
                    viewModel.fetchFoodDetails(rawValue)
                } else {
                    Toast.makeText(context, "No se pudo leer el código", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCanceledListener {
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error en escáner: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    // --- Fin Lógica del Escáner ---


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir alimento") },
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = uiState.foodName,
                onValueChange = { viewModel.onFoodNameChanged(it) }, // Conectado al VM
                label = { Text("Nombre del alimento") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChanged(it) }, // Conectado al VM
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text("g") }
                )
                Spacer(modifier = Modifier.width(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    viewModel.onCategoryChanged(selectionOption) // Conectado al VM
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.calories,
                onValueChange = { viewModel.onCaloriesChanged(it) }, // Conectado al VM
                label = { Text("Calorías (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Macronutrientes (opcional)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = uiState.protein, onValueChange = { viewModel.onProteinChanged(it) }, label = { Text("Proteínas (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = uiState.fat, onValueChange = { viewModel.onFatChanged(it) }, label = { Text("Grasas (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = uiState.carbs, onValueChange = { viewModel.onCarbsChanged(it) }, label = { Text("Carbohidratos (g)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            OutlinedButton(onClick = { /* TODO: Lógica para foto */ }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Foto del alimento")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Botón de escanear conectado
            OutlinedButton(onClick = { startScan() }, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Escanear código de barras")
            }

            // Indicador de carga
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones de Guardar y Cancelar
            Button(
                onClick = {
                    // La lógica de validación y guardado está ahora en el VM
                    viewModel.saveFoodItem(
                        onSaveSuccess = {
                            Toast.makeText(context, "${uiState.foodName} guardado!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                // La validación se basa en el estado del VM
                enabled = uiState.foodName.isNotBlank() && (uiState.quantity.toDoubleOrNull() ?: 0.0) > 0 && (uiState.calories.toIntOrNull() ?: 0) > 0 && !uiState.isLoading
            ) {
                Text("Guardar")
            }
            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}