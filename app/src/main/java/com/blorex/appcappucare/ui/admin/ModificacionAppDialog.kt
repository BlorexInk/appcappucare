package com.blorex.appcappucare.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.GamaColorApp
import com.blorex.appcappucare.repository.sync.HttpSyncClient
import com.blorex.appcappucare.repository.sync.SyncBackendRepository
import com.blorex.appcappucare.ui.components.CargarImagenProducto
import kotlinx.coroutines.launch

/**
 * Diálogo modal exclusivo para el usuario de **TI (`blorex`)** llamado "Modificación de App".
 *
 * Permite actualizar el logo corporativo desde la galería del teléfono y seleccionar la gama de colores
 * de la aplicación en tiempo real (Verde Salud, Rojo Pálido, Azul Claro, Azul Corporativo, Púrpura).
 * Al guardar, la app notifica y sincroniza la nueva marca inmediatamente con el servidor Backend.
 *
 * @param onDismiss Callback para cerrar el diálogo modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificacionAppDialog(
    onDismiss: () -> Unit
) {
    var logoUri by remember { mutableStateOf(ConfiguracionAppRepository.logoAppUri) }
    var gamaColor by remember { mutableStateOf(ConfiguracionAppRepository.gamaColorSeleccionada) }
    val scope = rememberCoroutineScope()

    // Launcher para cambiar el logo desde la galería del teléfono
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            logoUri = uri.toString()
            ConfiguracionAppRepository.actualizarLogo(logoUri)
            scope.launch {
                HttpSyncClient.enviarConfiguracionAlServidor(SyncBackendRepository.servidorUrlInput)
            }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modificación de App (Solo TI)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "Personalización del logo de marca, imágenes de portada y gama de colores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Sección de Cambio de Logo
                Text("1. Logo Corporativo de la Aplicación", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (logoUri.isNotBlank()) {
                                CargarImagenProducto(
                                    uriString = logoUri,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elegir Logo de Galería", fontSize = 12.sp)
                        }

                        if (logoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = {
                                    logoUri = ""
                                    ConfiguracionAppRepository.actualizarLogo("")
                                    scope.launch {
                                        HttpSyncClient.enviarConfiguracionAlServidor(SyncBackendRepository.servidorUrlInput)
                                    }
                                }
                            ) {
                                Text("Restablecer Logo", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Sección de Cambio de Gamas de Colores e Imagen de Portada Vincular
                Text("2. Selección de Gama de Colores e Imagen de Portada", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                val gamasConDetalle = listOf(
                    Triple(GamaColorApp.VERDE_SALUD, "🟢 Verde Salud y Blanco", Color(0xFF2E7D32)),
                    Triple(GamaColorApp.ROJO_PALIDO, "🔴 Rojo Pálido Médico (Portada: rojo.jpeg)", Color(0xFFC65353)),
                    Triple(GamaColorApp.AZUL_HOSPITALARIO, "🔵 Azul Claro Hospitalario (Portada: celeste.jpeg)", Color(0xFF0288D1)),
                    Triple(GamaColorApp.AZUL_CORPORATIVO, "🟦 Azul Corporativo (Portada: azul marino.jpeg)", Color(0xFF1565C0)),
                    Triple(GamaColorApp.PURPURA_SALUD, "🟣 Púrpura Salud (Portada: morado.jpeg)", Color(0xFF673AB7))
                )

                gamasConDetalle.forEach { (gama, label, colorMuestra) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                gamaColor = gama
                                ConfiguracionAppRepository.actualizarGamaColor(gama)
                                scope.launch {
                                    HttpSyncClient.enviarConfiguracionAlServidor(SyncBackendRepository.servidorUrlInput)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (gamaColor == gama) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (gamaColor == gama),
                                onClick = {
                                    gamaColor = gama
                                    ConfiguracionAppRepository.actualizarGamaColor(gama)
                                    scope.launch {
                                        HttpSyncClient.enviarConfiguracionAlServidor(SyncBackendRepository.servidorUrlInput)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = colorMuestra,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {}

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (gamaColor == gama) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        scope.launch {
                            HttpSyncClient.enviarConfiguracionAlServidor(SyncBackendRepository.servidorUrlInput)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aplicar y Guardar Cambios")
                }
            }
        }
    }
}
