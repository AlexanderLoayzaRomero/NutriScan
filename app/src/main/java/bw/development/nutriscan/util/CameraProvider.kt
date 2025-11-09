package bw.development.nutriscan.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraProvider {

    fun getTmpFileUri(context: Context): Uri {
        // 1. Crea un archivo temporal
        val tmpFile = File.createTempFile(
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_",
            ".jpg",
            context.cacheDir // Lo guarda en la caché de la app
        ).apply {
            createNewFile()
            deleteOnExit() // Se borra cuando la app se cierra
        }

        // 2. Obtiene la URI segura usando el FileProvider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Debe coincidir con el AndroidManifest
            tmpFile
        )
    }
}