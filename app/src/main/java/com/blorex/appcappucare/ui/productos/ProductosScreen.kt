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
import androidx.compose.material.icons.filled.Image
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
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.viewmodel.ProductosViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de Gestión de Productos / Inventario CRUD.
 *
 * Ofrece listado completo de insumos, barra de búsqueda, botón para agregar nuevo producto
 * con generación automática de código por nombre, campos para descripción, valor ($) e imágenes,
 * y opciones para editar y eliminar productos.
 *
 * @param modifier Modificador composable de diseño.
 * @param vm ViewModel de gestión de inventario.
 */
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
            onGuardar = { codigo, nombre, descripcion, valor, cantidad, unidad, min, imagenes ->
                val prod = Producto(
                    codigo = codigo,
                    nombre = nombre,
                    descripcion = descripcion,
                    valor = valor,
                    cantidadDisponible = cantidad,
                    unidadMedida = unidad,
                    nivelMinimo = min,
                    estadoStock = ProductosRepository.calcularEstadoStock(cantidad, min),
                    imagenes = imagenes
                )
                if (vm.productoEnEdicion != null) {
                    ProductosRepository.actualizarProducto(prod)
                } else {
                    ProductosRepository.agregarProducto(prod)
                }
                vm.cerrarFormulario()
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

/** Tarjeta individual de inventario. */
@Composable
fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

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
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Código: ${producto.codigo}  |  Precio: ${formatoMoneda.format(producto.valor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadoStockBadge(estado = producto.estadoStock)

                    Text(
                        text = "Stock: ${producto.cantidadDisponible} ${producto.unidadMedida} (Mín: ${producto.nivelMinimo})",
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

/** Badge de estado de stock. */
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

/** Formulario para agregar / editar producto con generación automática de código e imágenes. */
@Composable
fun FormularioProductoDialog(
    productoEdicion: Producto?,
    mensajeError: String?,
    onDismiss: () -> Unit,
    onGuardar: (codigo: String, nombre: String, descripcion: String, valor: Double, cantidad: Int, unidad: String, min: Int, imagenes: List<String>) -> Unit
) {
    var nombre by remember { mutableStateOf(productoEdicion?.nombre ?: "") }
    var codigo by remember { mutableStateOf(productoEdicion?.codigo ?: "") }
    var descripcion by remember { mutableStateOf(productoEdicion?.descripcion ?: "") }
    var valorText by remember { mutableStateOf(productoEdicion?.valor?.toInt()?.toString() ?: "1500") }
    var cantidadText by remember { mutableStateOf(productoEdicion?.cantidadDisponible?.toString() ?: "20") }
    var unidadMedida by remember { mutableStateOf(productoEdicion?.unidadMedida ?: "un") }
    var nivelMinimoText by remember { mutableStateOf(productoEdicion?.nivelMinimo?.toString() ?: "10") }
    var imagenesList by remember { mutableStateOf(productoEdicion?.imagenes ?: listOf("imagen_1.jpg")) }

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

                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { newNombre ->
                        nombre = newNombre
                        // Generación automática de código por el nombre si es un nuevo producto
                        if (!esEdicion) {
                            codigo = ProductosRepository.generarCodigoPredefinido(newNombre)
                        }
                    },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Código (Predefinido por Nombre)
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código Predefinido (Auto)") },
                    enabled = !esEdicion,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción Detallada") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Valor / Precio ($)
                OutlinedTextField(
                    value = valorText,
                    onValueChange = { valorText = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio / Valor ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cantidadText,
                        onValueChange = { cantidadText = it.filter { c -> c.isDigit() } },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unidadMedida,
                        onValueChange = { unidadMedida = it },
                        label = { Text("Unidad (ej: un)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nivelMinimoText,
                    onValueChange = { nivelMinimoText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nivel Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Imágenes asociadas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Imágenes asociadas: ${imagenesList.size}/4",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (imagenesList.size < 4) {
                        TextButton(
                            onClick = {
                                val nuevaNum = imagenesList.size + 1
                                imagenesList = imagenesList + "imagen_$nuevaNum.jpg"
                            }
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir foto")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val valNum = valorText.toDoubleOrNull() ?: 0.0
                val cant = cantidadText.toIntOrNull() ?: 0
                val min = nivelMinimoText.toIntOrNull() ?: 0
                onGuardar(codigo, nombre, descripcion, valNum, cant, unidadMedida, min, imagenesList)
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
