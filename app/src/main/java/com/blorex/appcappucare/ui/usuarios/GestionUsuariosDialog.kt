package com.blorex.appcappucare.ui.usuarios

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.ui.components.CargarImagenProducto

/**
 * Módulo de Gestión de Usuarios y Control de Acceso por Roles (RBAC).
 *
 * Exclusivo para el usuario de **TI (`blorex`)** y **Administradores**. Permite crear, modificar roles,
 * fotos de perfil, categorías de cargo, bodegas y eliminar usuarios registrados en la base de datos local y servidor Backend.
 *
 * @param onDismiss Callback para cerrar el módulo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosDialog(
    onDismiss: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    val usuariosList = AuthRepository.obtenerUsuarios()

    var usuarioEnEdicion by remember { mutableStateOf<Usuario?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    val usuariosFiltrados = usuariosList.filter {
        it.nombreCompleto.contains(busqueda, ignoreCase = true) ||
                it.username.contains(busqueda, ignoreCase = true) ||
                it.rol.contains(busqueda, ignoreCase = true)
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
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gestión de Usuarios (TI & Admin)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        placeholder = { Text("Buscar trabajador...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            usuarioEnEdicion = null
                            mostrarFormulario = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Usuario", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Trabajadores Registrados (${usuariosFiltrados.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = usuariosFiltrados,
                        key = { it.username }
                    ) { usr ->
                        TarjetaUsuarioItem(
                            usuario = usr,
                            onEditar = {
                                usuarioEnEdicion = usr
                                mostrarFormulario = true
                            },
                            onEliminar = {
                                usuarioAEliminar = usr
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal para agregar o editar usuario
    if (mostrarFormulario) {
        FormularioUsuarioModal(
            usuarioEdicion = usuarioEnEdicion,
            onDismiss = { mostrarFormulario = false },
            onGuardar = { u ->
                AuthRepository.guardarOActualizarUsuario(u)
                mostrarFormulario = false
            }
        )
    }

    // Confirmación de eliminación
    if (usuarioAEliminar != null) {
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de eliminar al usuario '${usuarioAEliminar?.username}' (${usuarioAEliminar?.nombreCompleto}) del sistema?") },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioAEliminar?.let { AuthRepository.eliminarUsuario(it.username) }
                        usuarioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { usuarioAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TarjetaUsuarioItem(
    usuario: Usuario,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (usuario.fotoPerfilUrl.isNotBlank()) {
                CargarImagenProducto(
                    uriString = usuario.fotoPerfilUrl,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = usuario.username.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "@${usuario.username}  |  ${usuario.categoriaCargo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Rol: ${usuario.rol}  |  ${usuario.ubicacionBodega}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(
                    onClick = onEliminar,
                    enabled = !usuario.username.equals("blorex", ignoreCase = true)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = if (usuario.username.equals("blorex", ignoreCase = true)) Color.Gray else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FormularioUsuarioModal(
    usuarioEdicion: Usuario?,
    onDismiss: () -> Unit,
    onGuardar: (Usuario) -> Unit
) {
    var username by remember { mutableStateOf(usuarioEdicion?.username ?: "") }
    var password by remember { mutableStateOf(usuarioEdicion?.password ?: "1234") }
    var nombre by remember { mutableStateOf(usuarioEdicion?.nombreCompleto ?: "") }
    var email by remember { mutableStateOf(usuarioEdicion?.email ?: "blorexink@gmail.com") }
    var rol by remember { mutableStateOf(usuarioEdicion?.rol ?: "Vendedor") }
    var categoria by remember { mutableStateOf(usuarioEdicion?.categoriaCargo ?: "Ventas y Mostrador") }
    var telefono by remember { mutableStateOf(usuarioEdicion?.telefono ?: "+56 9 1234 5678") }
    var direccion by remember { mutableStateOf(usuarioEdicion?.direccion ?: "Av. Principal 100") }
    var bodega by remember { mutableStateOf(usuarioEdicion?.ubicacionBodega ?: "Bodega Central XXXX") }
    var fotoPerfilUrl by remember { mutableStateOf(usuarioEdicion?.fotoPerfilUrl ?: "") }

    val esEdicion = usuarioEdicion != null

    // Launcher para asignar foto de perfil desde la galería (Solo TI)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fotoPerfilUrl = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Usuario" else "Nuevo Usuario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Picker Header for TI
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (fotoPerfilUrl.isNotBlank()) {
                        CargarImagenProducto(
                            uriString = fotoPerfilUrl,
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (username.isNotBlank()) username.take(2).uppercase() else "US",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Asignar Foto de Perfil", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de Usuario (@login)") },
                    enabled = !esEdicion,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Rol
                Text("Rol de Permisos:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                val roles = listOf("TI", "Administrador", "Gerente de Tienda", "Vendedor")
                roles.forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rol = r }
                    ) {
                        RadioButton(selected = (rol == r), onClick = { rol = r })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(r, fontSize = 13.sp)
                    }
                }

                // Categoría de Cargo
                Text("Categoría de Cargo:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                val categorias = listOf("Sistemas / TI", "Administración General", "Gerencia de Tienda", "Ventas y Mostrador")
                categorias.forEach { c ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoria = c }
                    ) {
                        RadioButton(selected = (categoria == c), onClick = { categoria = c })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(c, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
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
                    label = { Text("Ubicación Bodega") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = Usuario(
                        username = username.trim(),
                        password = password,
                        nombreCompleto = nombre.trim(),
                        rol = rol,
                        categoriaCargo = categoria,
                        email = email.trim(),
                        telefono = telefono.trim(),
                        direccion = direccion.trim(),
                        ubicacionBodega = bodega.trim(),
                        fotoPerfilUrl = fotoPerfilUrl,
                        fechaRegistro = usuarioEdicion?.fechaRegistro ?: "20/10/2026",
                        horaInicioSesion = usuarioEdicion?.horaInicioSesion ?: "Sesión Inactiva"
                    )
                    onGuardar(u)
                }
            ) {
                Text(if (esEdicion) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
