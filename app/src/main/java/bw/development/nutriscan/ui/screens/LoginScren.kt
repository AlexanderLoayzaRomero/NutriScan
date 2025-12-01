package bw.development.nutriscan.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bw.development.nutriscan.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Configuración de Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Se genera solo con el google-services.json
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Lanzador para el intent de Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Autenticar con Firebase
                scope.launch {
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    try {
                        Firebase.auth.signInWithCredential(credential).await()
                        isLoading = false
                        onLoginSuccess() // ¡Éxito! Navegar
                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(context, "Error en Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo Grande
            Image(
                painter = painterResource(id = R.drawable.ic_nutriscan_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido a NutriScan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para guardar tu progreso",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Botón de Google
                Button(
                    onClick = {
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    // Aquí podrías poner un icono de Google si tienes uno en drawable
                    Text(text = "Continuar con Google", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}