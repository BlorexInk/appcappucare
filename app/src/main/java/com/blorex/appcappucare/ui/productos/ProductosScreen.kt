
package com.blorex.appcappucare.ui.productos

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.viewmodel.CampoProducto
import com.blorex.appcappucare.viewmodel.ProductoFormulario
import com.blorex.appcappucare.viewmodel.ProductosViewModel
import java.io.File
import java.text.NumberFormat
import java.util.Locale

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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar producto"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

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
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = vm.busqueda,
                onValueChange = { vm.busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar por nombre o código...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (productosList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (vm.busqueda.isNotBlank()) {
                            "No se encontraron productos para \"${vm.busqueda}\""
                        } else {
                            "No hay productos registrados"
                        },
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
                            onEditar = {
                                vm.abrirFormularioEditar(producto)
                            },
                            onEliminar = {
                                vm.solicitarEliminacion(producto)
                            }
                        )
                    }
                }
            }
        }
    }

    if (vm.mostrarDialogoFormulario) {
        FormularioProductoDialog(
            productoEdicion = vm.productoEnEdicion,
            mensajeError = vm.mensajeError,
            errores = vm.erroresFormulario,
            onCampoEditado = vm::limpiarError,
            onDismiss = { vm.cerrarFormulario() },
            onGuardar = { formulario -> vm.guardarProducto(formulario) }
        )
    }

    if (vm.mostrarDialogoEliminar) {
        vm.productoAEliminar?.let { producto ->

            AlertDialog(
                onDismissRequest = {
                    vm.cancelarEliminacion()
                },
                title = {
                    Text("Eliminar Producto")
                },
                text = {
                    Text(
                        "¿Deseas eliminar permanentemente el producto " +
                                "'${producto.nombre}' (${producto.codigo})?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.confirmarEliminacion()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            vm.cancelarEliminacion()
                        }
                    ) {
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
    val context = LocalContext.current

    var mostrarDetalles by remember(producto.codigo) {
        mutableStateOf(false)
    }

    val formatoMoneda = NumberFormat.getCurrencyInstance(
        Locale("es", "CL")
    ).apply {
        maximumFractionDigits = 0
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            mostrarDetalles = !mostrarDetalles
                        }
                ) {

                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Código: ${producto.codigo} | " +
                                "Precio: ${formatoMoneda.format(producto.valor)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        EstadoStockBadge(
                            estado = producto.estadoStock
                        )

                        Text(
                            text = "Stock: ${producto.cantidadDisponible} " +
                                    "${producto.unidadMedida} " +
                                    "(Mín: ${producto.nivelMinimo})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row {

                    IconButton(
                        onClick = {
                            val mensaje = buildString {
                                appendLine(producto.nombre)
                                appendLine("Código: ${producto.codigo}")
                                appendLine(
                                    "Precio: ${formatoMoneda.format(producto.valor)}"
                                )
                                appendLine(
                                    "Stock: ${producto.cantidadDisponible} " +
                                            producto.unidadMedida
                                )
                                appendLine(producto.descripcion)
                            }

                            val intent = Intent(
                                Intent.ACTION_SEND
                            ).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    mensaje
                                )
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Compartir producto"
                                )
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir producto"
                        )
                    }

                    IconButton(
                        onClick = onEditar
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar producto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onEliminar
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar producto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = mostrarDetalles ||
                        !producto.fotoUri.isNullOrBlank()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    )
                ) {

                    FotoProducto(
                        uriTexto = producto.fotoUri
                    )

                    if (
                        mostrarDetalles &&
                        producto.descripcion.isNotBlank()
                    ) {
                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = producto.descripcion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FotoProducto(
    uriTexto: String?
) {
    if (uriTexto.isNullOrBlank()) return

    val context = LocalContext.current

    val bitmap = remember(uriTexto) {
        try {
            context.contentResolver
                .openInputStream(Uri.parse(uriTexto))
                ?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                        ?.asImageBitmap()
                }
        } catch (_: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Fotografía del producto",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun EstadoStockBadge(
    estado: EstadoStock
) {
    val (colorFondo, colorTexto, etiqueta) = when (estado) {

        EstadoStock.DISPONIBLE -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Disponible"
        )

        EstadoStock.STOCK_BAJO -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFF57F17),
            "Stock Bajo"
        )

        EstadoStock.AGOTADO -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "Agotado"
        )
    }

    Box(
        modifier = Modifier
            .background(
                colorFondo,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 2.dp
            )
    ) {
        Text(
            text = etiqueta,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FormularioProductoDialog(
    productoEdicion: Producto?,
    mensajeError: String?,
    errores: Map<CampoProducto, String>,
    onCampoEditado: (CampoProducto) -> Unit,
    onDismiss: () -> Unit,
    onGuardar: (ProductoFormulario) -> Unit
) {
    val context = LocalContext.current

    val esEdicion = productoEdicion != null

    var nombre by remember(productoEdicion?.codigo) {
        mutableStateOf(productoEdicion?.nombre ?: "")
    }

    var codigo by remember(productoEdicion?.codigo) {
        mutableStateOf(productoEdicion?.codigo ?: "")
    }

    var descripcion by remember(productoEdicion?.codigo) {
        mutableStateOf(productoEdicion?.descripcion ?: "")
    }

    var valorText by remember(productoEdicion?.codigo) {
        mutableStateOf(
            productoEdicion?.valor?.toInt()?.toString() ?: "1500"
        )
    }

    var cantidadText by remember(productoEdicion?.codigo) {
        mutableStateOf(
            productoEdicion?.cantidadDisponible?.toString() ?: "20"
        )
    }

    var unidadMedida by remember(productoEdicion?.codigo) {
        mutableStateOf(
            productoEdicion?.unidadMedida ?: "un"
        )
    }

    var nivelMinimoText by remember(productoEdicion?.codigo) {
        mutableStateOf(
            productoEdicion?.nivelMinimo?.toString() ?: "10"
        )
    }

    var imagenesList by remember(productoEdicion?.codigo) {
        mutableStateOf(
            productoEdicion?.imagenes ?: emptyList()
        )
    }

    var fotoUri by remember(productoEdicion?.codigo) {
        mutableStateOf(productoEdicion?.fotoUri)
    }

    var fotoPendiente by remember {
        mutableStateOf<Uri?>(null)
    }

    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->

        if (exito) {
            fotoUri = fotoPendiente?.toString()
        }

        fotoPendiente = null
    }

    fun abrirCamara() {
        try {
            val carpeta = File(
                context.filesDir,
                "imagenes_productos"
            ).apply {
                mkdirs()
            }

            val archivo = File.createTempFile(
                "producto_",
                ".jpg",
                carpeta
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            fotoPendiente = uri

            tomarFoto.launch(uri)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se pudo abrir la cámara: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (esEdicion) {
                    "Editar Producto"
                } else {
                    "Nuevo Producto"
                }
            )
        },
        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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

                CampoProductoTexto(
                    valor = nombre,
                    onValor = { nuevoNombre ->
                        nombre = nuevoNombre
                        onCampoEditado(CampoProducto.NOMBRE)
                        if (!esEdicion) {
                            codigo = ProductosRepository.generarCodigoPredefinido(nuevoNombre)
                            onCampoEditado(CampoProducto.CODIGO)
                        }
                    },
                    etiqueta = "Nombre del Producto",
                    icono = Icons.Default.Inventory2,
                    error = errores[CampoProducto.NOMBRE],
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = codigo,
                    onValor = {
                        codigo = it
                        onCampoEditado(CampoProducto.CODIGO)
                    },
                    etiqueta = "Código Predefinido (Auto)",
                    icono = Icons.Default.QrCode,
                    error = errores[CampoProducto.CODIGO],
                    habilitado = !esEdicion,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = descripcion,
                    onValor = { descripcion = it },
                    etiqueta = "Descripción Detallada",
                    icono = Icons.Default.Description,
                    error = null,
                    unaLinea = false,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = valorText,
                    onValor = { texto ->
                        valorText = texto.filter { it.isDigit() }
                        onCampoEditado(CampoProducto.PRECIO)
                    },
                    etiqueta = "Precio / Valor ($)",
                    icono = Icons.Default.AttachMoney,
                    error = errores[CampoProducto.PRECIO],
                    teclado = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = cantidadText,
                    onValor = { texto ->
                        cantidadText = texto.filter { it.isDigit() }
                        onCampoEditado(CampoProducto.STOCK)
                    },
                    etiqueta = "Stock",
                    icono = Icons.Default.Layers,
                    error = errores[CampoProducto.STOCK],
                    teclado = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = unidadMedida,
                    onValor = {
                        unidadMedida = it
                        onCampoEditado(CampoProducto.UNIDAD)
                    },
                    etiqueta = "Unidad (ej: un)",
                    icono = Icons.Default.Straighten,
                    error = errores[CampoProducto.UNIDAD],
                    modifier = Modifier.fillMaxWidth()
                )

                CampoProductoTexto(
                    valor = nivelMinimoText,
                    onValor = { texto ->
                        nivelMinimoText = texto.filter { it.isDigit() }
                        onCampoEditado(CampoProducto.NIVEL_MINIMO)
                    },
                    etiqueta = "Nivel Mínimo",
                    icono = Icons.Default.NotificationsActive,
                    error = errores[CampoProducto.NIVEL_MINIMO],
                    teclado = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Fotografía del producto",
                    fontWeight = FontWeight.SemiBold
                )

                FotoProducto(
                    uriTexto = fotoUri
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedButton(
                        onClick = {
                            abrirCamara()
                        }
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            if (fotoUri == null) {
                                "Tomar foto"
                            } else {
                                "Cambiar foto"
                            }
                        )
                    }

                    if (fotoUri != null) {
                        TextButton(
                            onClick = {
                                fotoUri = null
                            }
                        ) {
                            Text("Quitar foto")
                        }
                    }
                }

                if (imagenesList.isNotEmpty()) {
                    Text(
                        text = "Imágenes anteriores: ${imagenesList.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {

            Button(
                onClick = {
                    onGuardar(
                        ProductoFormulario(
                            codigo = codigo,
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = valorText,
                            stock = cantidadText,
                            unidad = unidadMedida,
                            nivelMinimo = nivelMinimoText,
                            imagenes = imagenesList,
                            fotoUri = fotoUri
                        )
                    )
                }
            ) {
                Text(
                    if (esEdicion) {
                        "Actualizar"
                    } else {
                        "Guardar"
                    }
                )
            }
        },
        dismissButton = {

            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Campo de texto del formulario de producto con ícono, estado de error y mensaje visual.
 * No contiene reglas de validación: solo muestra el error que entrega el ViewModel.
 */
@Composable
private fun CampoProductoTexto(
    valor: String,
    onValor: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    unaLinea: Boolean = true,
    teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        enabled = habilitado,
        singleLine = unaLinea,
        maxLines = if (unaLinea) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = modifier
    )
}
