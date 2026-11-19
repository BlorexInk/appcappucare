package com.blorex.appcappucare.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Componente Composable encargado de cargar y renderizar de forma segura fotos seleccionadas
 * desde la Galería del dispositivo (URIs `content://` o `file://`), desde la carpeta de Assets (`asset://`) o URLs remotas.
 *
 * @param uriString URI o ruta de la imagen.
 * @param modifier Modificador de diseño.
 * @param contentDescription Descripción de accesibilidad.
 */
@Composable
fun CargarImagenProducto(
    uriString: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    var imageBitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }
    var errorCarga by remember(uriString) { mutableStateOf(false) }

    LaunchedEffect(uriString) {
        if (uriString.isBlank()) {
            errorCarga = true
            return@LaunchedEffect
        }

        try {
            if (uriString.startsWith("asset://") || uriString.startsWith("assets/")) {
                val assetPath = uriString.removePrefix("asset://").removePrefix("assets/")
                val inputStream = context.assets.open(assetPath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    imageBitmap = bitmap.asImageBitmap()
                    errorCarga = false
                } else {
                    errorCarga = true
                }
            } else if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    if (bitmap != null) {
                        imageBitmap = bitmap.asImageBitmap()
                        errorCarga = false
                    } else {
                        errorCarga = true
                    }
                } else {
                    errorCarga = true
                }
            } else {
                errorCarga = true
            }
        } catch (e: Exception) {
            errorCarga = true
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null && !errorCarga) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Icono / Marcador de posición por defecto
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
