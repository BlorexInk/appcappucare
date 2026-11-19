package com.blorex.appcappucare.ui.usuarios

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.SolicitudModificacion
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.repository.SolicitudesRepository
import com.blorex.appcappucare.ui.components.CargarImagenProducto

/**
 * Diálogo modal para visualizar el Perfil de Usuario Completo.
 *
 * Para empleados no-TI, los datos son de solo lectura y ofrece la opción de **"Enviar Solicitud de Cambio a TI"**.
 * Al enviar la solicitud, la app notifica internamente a TI (`blorex`) y envía copia por correo a **`blorexink@gmail.com`**.
 *
 * @param usuario Usuario a consultar.
 * @param usuarioLogueado Usuario autenticado en uso.
 * @param onDismiss Callback para cerrar la ventana modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioDialog(
    usuario: Usuario,
    usuarioLogueado: Usuario,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val esTI = usuarioLogueado.rol.equals("TI", ignoreCase = true)

    var modoEdicion by remember { mutableStateOf(false) }
    var mostrarModalSolicitud by remember { mutableStateOf(false) }
    var mensajeConfirmacionSolicitud by remember { mutableStateOf<String?>(null) }

    var nombreCompleto by remember { mutableStateOf(usuario.nombreCompleto) }
    var telefono by remember { mutableStateOf(usuario.telefono) }
    var direccion by remember { mutableStateOf(usuario.direccion) }
    var bodega by remember { mutableStateOf(usuario.ubicacionBodega) }
    var fotoUrl by remember { mutableStateOf(usuario.fotoPerfilUrl) }
    var categoriaCargo by remember { mutableStateOf(usuario.categoriaCargo) }

    // Launcher para cambiar foto de perfil desde la galería (Exclusivo para TI)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fotoUrl = uri.toString()
            val uActualizado = usuario.copy(fotoPerfilUrl = fotoUrl)
            AuthRepository.guardarOActualizarUsuario(uActualizado)
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
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }

                    // Solo el usuario TI (blorex) puede editar directamente los datos
                    if (esTI) {
                        IconButton(onClick = { modoEdicion = !modoEdicion }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Photo Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (fotoUrl.isNotBlank()) {
                        CargarImagenProducto(
                            uriString = fotoUrl,
                            modifier = Modifier
                                .size(90.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = usuario.username.take(2).uppercase(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Permitir actualizar foto EXCLUSIVAMENTE para TI (blorex)
                    if (esTI) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { galleryLauncher.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Cambiar Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = nombreCompleto,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "@${usuario.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role & Category Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = usuario.rol,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = categoriaCargo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Session Active Time Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Estado y Horario de Activación",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (usuario.horaInicioSesion.isBlank()) "Sesión Activa Ahora" else "Sesión activa: ${usuario.horaInicioSesion}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje de éxito si envió solicitud
                mensajeConfirmacionSolicitud?.let { msg ->
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Fields
                if (modoEdicion && esTI) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = nombreCompleto,
                            onValueChange = { nombreCompleto = it },
                            label = { Text("Nombre Completo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono de Contacto") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bodega,
                            onValueChange = { bodega = it },
                            label = { Text("Ubicación de Bodega") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val uActualizado = usuario.copy(
                                    nombreCompleto = nombreCompleto,
                                    telefono = telefono,
                                    direccion = direccion,
                                    ubicacionBodega = bodega,
                                    fotoPerfilUrl = fotoUrl,
                                    categoriaCargo = categoriaCargo
                                )
                                AuthRepository.guardarOActualizarUsuario(uActualizado)
                                modoEdicion = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Cambios de Perfil")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilaPerfilInfo(icon = Icons.Default.Email, titulo = "Correo Electrónico", valor = usuario.email)
                        FilaPerfilInfo(icon = Icons.Default.Phone, titulo = "Teléfono", valor = telefono)
                        FilaPerfilInfo(icon = Icons.Default.Home, titulo = "Dirección", valor = direccion)
                        FilaPerfilInfo(icon = Icons.Default.Business, titulo = "Ubicación Bodega", valor = bodega)
                        FilaPerfilInfo(icon = Icons.Default.Badge, titulo = "Fecha de Registro", valor = usuario.fechaRegistro)

                        // Si no es TI, ofrecer el botón de solicitar cambios
                        if (!esTI) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { mostrarModalSolicitud = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar Solicitud de Cambio a TI")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para redactar y enviar solicitud de modificación a TI (blorexink@gmail.com)
    if (mostrarModalSolicitud) {
        var asuntoInput by remember { mutableStateOf("Solicitud de actualización de datos") }
        var detalleInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarModalSolicitud = false },
            title = { Text("Solicitar Modificación de Datos a TI") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "La modificación de datos de trabajadores es gestionada exclusivamente por el departamento de TI. Se notificará en la app y se enviará una copia por correo a blorexink@gmail.com:",
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = asuntoInput,
                        onValueChange = { asuntoInput = it },
                        label = { Text("Asunto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = detalleInput,
                        onValueChange = { detalleInput = it },
                        label = { Text("Detalle de la modificación requerida") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (asuntoInput.isNotBlank() && detalleInput.isNotBlank()) {
                            val nuevaSol = SolicitudModificacion(
                                usernameEmpleado = usuario.username,
                                nombreEmpleado = usuario.nombreCompleto,
                                rolEmpleado = usuario.rol,
                                asunto = asuntoInput.trim(),
                                detalle = detalleInput.trim()
                            )
                            SolicitudesRepository.crearSolicitud(nuevaSol)

                            // Disparar Intent de correo de aviso a blorexink@gmail.com
                            try {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:blorexink@gmail.com")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("blorexink@gmail.com", usuario.email))
                                    putExtra(Intent.EXTRA_SUBJECT, "[Solicitud TI AppCappuCare] ${asuntoInput.trim()}")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Solicitud enviada por: ${usuario.nombreCompleto} (@${usuario.username})\nRol: ${usuario.rol}\nCorreo: ${usuario.email}\n\nAsunto: ${asuntoInput.trim()}\n\nDetalle:\n${detalleInput.trim()}"
                                    )
                                }
                                onDismiss()
                                context.startActivity(Intent.createChooser(emailIntent, "Enviar aviso por correo a TI (blorexink@gmail.com)..."))
                            } catch (e: Exception) {
                                // Fallback
                            }

                            mostrarModalSolicitud = false
                            mensajeConfirmacionSolicitud = "¡Solicitud enviada a TI con éxito! Registrada en sistema y enviada por correo a blorexink@gmail.com."
                        }
                    }
                ) {
                    Text("Enviar Solicitud")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarModalSolicitud = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FilaPerfilInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = titulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
