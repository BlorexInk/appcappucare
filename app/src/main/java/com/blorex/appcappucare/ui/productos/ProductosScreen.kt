package com.blorex.appcappucare.ui.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.viewmodel.ProductosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    modifier: Modifier = Modifier,
    vm: ProductosViewModel = viewModel()
) {
    val productosList = vm.productos

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.abrirFormularioNuevo() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header & Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventario de Productos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Total: ${productosList.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = vm.busqueda,
                onValueChange = { vm.busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o código...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Products List
            if (productosList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (vm.busqueda.isNotBlank()) "No se encontraron productos para \"${vm.busqueda}\""
                        else "No hay productos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = productosList,
                        key = { it.codigo }
                    ) { producto ->
                        ProductoCard(
                            producto = producto,
                            onEditar = { vm.abrirFormularioEditar(producto) },
                            onEliminar = { vm.solicitarEliminacion(producto) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (vm.mostrarDialogoFormulario) {
        FormularioProductoDialog(
            productoEdicion = vm.productoEnEdicion,
            mensajeError = vm.mensajeError,
            onDismiss = { vm.cerrarFormulario() },
            onGuardar = { codigo, nombre, cantidad, unidad, min ->
                vm.guardarProducto(codigo, nombre, cantidad, unidad, min)
            }
        )
    }

    if (vm.mostrarDialogoEliminar) {
        vm.productoAEliminar?.let { prod ->
            AlertDialog(
                onDismissRequest = { vm.cancelarEliminacion() },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Deseas eliminar permanentemente el producto '${prod.nombre}' (${prod.codigo})?") },
                confirmButton = {
                    Button(
                        onClick = { vm.confirmarEliminacion() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { vm.cancelarEliminacion() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Código: ${producto.codigo}  |  Unidad: ${producto.unidadMedida}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock badge
                    EstadoStockBadge(estado = producto.estadoStock)

                    Text(
                        text = "Stock: ${producto.cantidadDisponible} (Mín: ${producto.nivelMinimo})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Row {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Producto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Producto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoStockBadge(estado: EstadoStock) {
    val (bgColor, textColor, label) = when (estado) {
        EstadoStock.DISPONIBLE -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Disponible")
        EstadoStock.STOCK_BAJO -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Stock Bajo")
        EstadoStock.AGOTADO -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Agotado")
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FormularioProductoDialog(
    productoEdicion: Producto?,
    mensajeError: String?,
    onDismiss: () -> Unit,
    onGuardar: (codigo: String, nombre: String, cantidad: Int, unidad: String, min: Int) -> Unit
) {
    var codigo by remember { mutableStateOf(productoEdicion?.codigo ?: "") }
    var nombre by remember { mutableStateOf(productoEdicion?.nombre ?: "") }
    var cantidadText by remember { mutableStateOf(productoEdicion?.cantidadDisponible?.toString() ?: "0") }
    var unidadMedida by remember { mutableStateOf(productoEdicion?.unidadMedida ?: "un") }
    var nivelMinimoText by remember { mutableStateOf(productoEdicion?.nivelMinimo?.toString() ?: "10") }

    val esEdicion = productoEdicion != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Producto" else "Nuevo Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mensajeError != null) {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código") },
                    enabled = !esEdicion,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cantidadText,
                        onValueChange = { cantidadText = it.filter { c -> c.isDigit() } },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unidadMedida,
                        onValueChange = { unidadMedida = it },
                        label = { Text("Unidad (ej: un, caja)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nivelMinimoText,
                    onValueChange = { nivelMinimoText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nivel Mínimo de Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cant = cantidadText.toIntOrNull() ?: 0
                val min = nivelMinimoText.toIntOrNull() ?: 0
                onGuardar(codigo, nombre, cant, unidadMedida, min)
            }) {
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
