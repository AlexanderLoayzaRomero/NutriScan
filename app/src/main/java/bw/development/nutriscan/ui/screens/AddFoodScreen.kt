package bw.development.nutriscan.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bw.development.nutriscan.ui.viewmodels.AddFoodViewModel
import bw.development.nutriscan.ui.viewmodels.ViewModelFactory
import bw.development.nutriscan.util.CameraProvider
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import kotlinx.coroutines.launch // <-- Asegúrate de tener este import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onNavigateBack: () -> Unit,
    startScanNow: Boolean,

    editingItemId: Int,
    viewModel: AddFoodViewModel = viewModel(
        factory = ViewModelFactory(context = LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("Desayuno", "Comida", "Cena", "Snack")
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Lógica del Escáner (sin cambios) ---
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val scanner = GmsBarcodeScanning.getClient(context, options)

    val startScan = {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (rawValue != null) {
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

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Lanzador para la CÁMARA
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Si la foto se tomó, guarda la URI en el ViewModel
            viewModel.onImagePicked(tempImageUri?.toString())

            tempImageUri?.let { uri ->
                viewModel.analyzeImage(context, uri)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Guardamos la URI para mostrarla
            viewModel.onImagePicked(uri.toString())
            // ¡Y la enviamos directo a la IA!
            viewModel.analyzeImage(context, uri)
        }
    }

    // 2. Lanzador para el PERMISO de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, crear URI y lanzar cámara
            val uri = CameraProvider.getTmpFileUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            // Permiso denegado, mostrar mensaje
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permiso de cámara necesario para esta función.")
            }
        }
    }

    // 3. Función del botón de cámara
    val onTakePhotoClick = {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // --- Lógica del Snackbar (sin cambios) ---
    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    // --- AÑADIR ESTE BLOQUE ---
    // Lanza el escáner automáticamente si se lo pedimos
    LaunchedEffect(key1 = startScanNow) {
        if (startScanNow) {
            startScan()
        }
    }

    // Carga el item para editar, pero solo una vez
    LaunchedEffect(key1 = editingItemId) {
        if (editingItemId != 0) {
            viewModel.loadFoodItem(editingItemId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingItemId != 0) "Editar alimento" else "Añadir alimento") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // CAMBIO: Usamos un Box para superponer el spinner de carga grande
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                AnimatedVisibility(visible = uiState.imageUri != null) {
                    AsyncImage(
                        model = uiState.imageUri,
                        contentDescription = "Foto del alimento",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Ya no usamos ExposedDropdownMenuBox.
                // Es un Column que contiene el TextField y la lista de resultados.
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.foodName,
                        onValueChange = { viewModel.onFoodNameChanged(it) },
                        label = { Text("Nombre del alimento") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (uiState.isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    )

                    // --- NUEVO: Lógica para mostrar la lista de resultados ---
                    // Esta lista aparece *debajo* del TextField, sin robar foco.
                    val showResults = uiState.foodName.length >= 3 && !uiState.isSearching && !uiState.isLoading

                    AnimatedVisibility(visible = showResults) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column {
                                if (uiState.searchResults.isEmpty()) {
                                    Text(
                                        text = "No se encontraron resultados",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    uiState.searchResults.forEach { product ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.onSearchResultSelected(product)
                                                }
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = product.productName ?: "Nombre desconocido",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // --- FIN del bloque de búsqueda ---


                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = { viewModel.onQuantityChanged(it) },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        suffix = { Text("g") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Dropdown de Categoría (sin cambios)
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        viewModel.onCategoryChanged(selectionOption)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.calories,
                    onValueChange = { viewModel.onCaloriesChanged(it) },
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

                OutlinedButton(
                    onClick = { onTakePhotoClick() }, // <-- Llama a nuestra función
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    // Cambia el texto si ya hay una foto
                    Text(if (uiState.imageUri != null) "Volver a tomar foto" else "Foto del alimento")
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        // Abre la galería solo para fotos
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Seleccionar de Galería")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = { startScan() }, modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Escanear código de barras")
                }

                Spacer(modifier = Modifier.weight(1f)) // Empuja los botones hacia abajo

                Button(
                    onClick = {
                        viewModel.saveFoodItem(
                            onSaveSuccess = {
                                val message = if (editingItemId != 0) "¡Guardado!" else "${uiState.foodName} guardado!"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.foodName.isNotBlank() && (uiState.quantity.toDoubleOrNull() ?: 0.0) > 0 && (uiState.calories.toIntOrNull() ?: 0) > 0 && !uiState.isLoading && !uiState.isSearching
                ) {
                    Text(if (editingItemId != 0) "Guardar Cambios" else "Guardar")
                }
                TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            } // Fin Column principal

            // Spinner grande (para escáner)
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } // Fin Box
    }
}