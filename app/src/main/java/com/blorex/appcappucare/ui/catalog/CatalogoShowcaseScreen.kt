package com.blorex.appcappucare.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.FavoritosRepository
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.ui.asistente.AsistenteIADialog
import com.blorex.appcappucare.ui.components.CargarImagenProducto
import com.blorex.appcappucare.utils.CompartirUtils
import com.blorex.appcappucare.viewmodel.VentaViewModel
import java.text.NumberFormat
import java.util.*

/** Opciones de ordenamiento disponibles para el catálogo. */
enum class OrdenamientoCatalogo {
    /** Orden alfabético de la A a la Z. */
    ALFABETICO_AZ,
    /** Orden alfabético de la Z a la A. */
    ALFABETICO_ZA,
    /** Orden de precio de mayor a menor. */
    PRECIO_MAYOR_MENOR,
    /** Orden de precio de menor a mayor. */
    PRECIO_MENOR_MAYOR
}

/**
 * Pantalla del Catálogo de Productos (Showcase).
 *
 * Muestra las tarjetas de los productos optimizando el espacio en pantalla sin márgenes dobles,
 * con selector de cantidad deseada (+/-), botón de compartir e imagen de portada por color.
 *
 * @param modifier Modificador composable de diseño recibido del Scaffold principal.
 * @param ventaViewModel ViewModel que gestiona las transacciones y carrito de compras.
 */
@Composable
fun CatalogoShowcaseScreen(
    modifier: Modifier = Modifier,
    ventaViewModel: VentaViewModel
) {
    var busqueda by remember { mutableStateOf("") }
    var esVistaCuadricula by remember { mutableStateOf(true) }
    var ordenActual by remember { mutableStateOf(OrdenamientoCatalogo.ALFABETICO_AZ) }
    var mostrarMenuOrden by remember { mutableStateOf(false) }
    var mostrarAsistente by remember { mutableStateOf(false) }

    var productoDetalleSeleccionado by remember { mutableStateOf<Producto?>(null) }

    val todosLosProductos = ProductosRepository.getProductosFicticios()
    val snackbarHostState = remember { SnackbarHostState() }
    val portadaActualUri = ConfiguracionAppRepository.obtenerPortadaActual()

    val productosFiltrados = todosLosProductos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) || it.codigo.contains(busqueda, ignoreCase = true)
    }

    val productosOrdenados = when (ordenActual) {
        OrdenamientoCatalogo.ALFABETICO_AZ -> productosFiltrados.sortedBy { it.nombre }
        OrdenamientoCatalogo.ALFABETICO_ZA -> productosFiltrados.sortedByDescending { it.nombre }
        OrdenamientoCatalogo.PRECIO_MAYOR_MENOR -> productosFiltrados.sortedByDescending { it.valor }
        OrdenamientoCatalogo.PRECIO_MENOR_MAYOR -> productosFiltrados.sortedBy { it.valor }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Header Banner Card con Imagen de Portada vinculada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catálogo de Productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Insumos médicos hospitalarios con garantía y despacho",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Render Banner Cover Image
                    CargarImagenProducto(
                        uriString = portadaActualUri,
                        modifier = Modifier
                            .size(54.dp)
                            .padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Asistente de compras con IA
            FilledTonalButton(
                onClick = { mostrarAsistente = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asistente de compras con IA")
            }
            if (mostrarAsistente) {
                AsistenteIADialog(
                    ventaViewModel = ventaViewModel,
                    onCerrar = { mostrarAsistente = false }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Warning Banner
            ventaViewModel.mensajeAdvertenciaStock.value?.let { advertencia ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta Stock",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = advertencia,
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { ventaViewModel.limpiarAdvertenciaStock() }) {
                            Text("OK", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar & Filter Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar insumo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Sort Filter Button
                Box {
                    IconButton(onClick = { mostrarMenuOrden = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }

                    DropdownMenu(
                        expanded = mostrarMenuOrden,
                        onDismissRequest = { mostrarMenuOrden = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nombre (A - Z)") },
                            onClick = {
                                ordenActual = OrdenamientoCatalogo.ALFABETICO_AZ
                                mostrarMenuOrden = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nombre (Z - A)") },
                            onClick = {
                                ordenActual = OrdenamientoCatalogo.ALFABETICO_ZA
                                mostrarMenuOrden = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Precio: Mayor a Menor") },
                            onClick = {
                                ordenActual = OrdenamientoCatalogo.PRECIO_MAYOR_MENOR
                                mostrarMenuOrden = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Precio: Menor a Mayor") },
                            onClick = {
                                ordenActual = OrdenamientoCatalogo.PRECIO_MENOR_MAYOR
                                mostrarMenuOrden = false
                            }
                        )
                    }
                }

                // Grid / List View Toggle Button
                IconButton(onClick = { esVistaCuadricula = !esVistaCuadricula }) {
                    Icon(
                        imageVector = if (esVistaCuadricula) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                        contentDescription = "Cambiar Vista"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Productos Disponibles (${productosOrdenados.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (productosOrdenados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos disponibles con ese criterio de búsqueda.")
                }
            } else {
                if (esVistaCuadricula) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = productosOrdenados,
                            key = { it.codigo }
                        ) { producto ->
                            val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)

                            ProductoShowcaseCard(
                                producto = producto,
                                stockActual = stockActual,
                                onClickDetalle = { productoDetalleSeleccionado = producto },
                                onAgregarAlCarritoConCantidad = { cant -> ventaViewModel.agregarAlCarrito(producto, cant) },
                                onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = productosOrdenados,
                            key = { it.codigo }
                        ) { producto ->
                            val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)

                            ProductoItemLista(
                                producto = producto,
                                stockActual = stockActual,
                                onClickDetalle = { productoDetalleSeleccionado = producto },
                                onAgregarAlCarritoConCantidad = { cant -> ventaViewModel.agregarAlCarrito(producto, cant) },
                                onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Modal de Detalle de Producto al presionar una tarjeta
    productoDetalleSeleccionado?.let { producto ->
        val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)
        DetalleProductoDialog(
            producto = producto,
            stockActual = stockActual,
            onDismiss = { productoDetalleSeleccionado = null },
            onAgregarAlCarritoConCantidad = { cant -> ventaViewModel.agregarAlCarrito(producto, cant) },
            onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
        )
    }
}

/**
 * Tarjeta de presentación en modo Cuadrícula (Grid) con selector de cantidad deseada (+/-) y botón de compartir.
 */
@Composable
fun ProductoShowcaseCard(
    producto: Producto,
    stockActual: Int,
    onClickDetalle: () -> Unit,
    onAgregarAlCarritoConCantidad: (cantidad: Int) -> Unit,
    onToggleFavorito: () -> Unit
) {
    val context = LocalContext.current
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }
    val primeraImagen = producto.imagenes.firstOrNull() ?: ""

    var cantidadDeseada by remember { mutableStateOf(1) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetalle() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CargarImagenProducto(
                    uriString = primeraImagen,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp))
                )

                Row {
                    // Botón Compartir 📲
                    IconButton(
                        onClick = { CompartirUtils.compartirProducto(context, producto) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorito,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (esFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2
            )

            Text(
                text = "Precio: ${formatoMoneda.format(producto.valor)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(3.dp))

            Surface(
                color = when {
                    estaAgotado -> Color(0xFFFFEBEE)
                    producto.estadoStock == EstadoStock.STOCK_BAJO -> Color(0xFFFFF8E1)
                    else -> Color(0xFFE8F5E9)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = when {
                            estaAgotado -> Color(0xFFC62828)
                            producto.estadoStock == EstadoStock.STOCK_BAJO -> Color(0xFFF57F17)
                            else -> Color(0xFF2E7D32)
                        },
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (estaAgotado) "AGOTADO" else "Stock: $stockActual ${producto.unidadMedida}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            estaAgotado -> Color(0xFFC62828)
                            producto.estadoStock == EstadoStock.STOCK_BAJO -> Color(0xFFF57F17)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Selector de Cantidad Deseada (+ / -)
            if (!estaAgotado) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (cantidadDeseada > 1) cantidadDeseada -= 1 },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(12.dp))
                        }
                    }

                    Text(
                        text = "$cantidadDeseada",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (cantidadDeseada < stockActual) cantidadDeseada += 1 },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Button(
                onClick = { onAgregarAlCarritoConCantidad(cantidadDeseada) },
                enabled = !estaAgotado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(if (estaAgotado) "Agotado" else "Agregar ($cantidadDeseada)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Tarjeta de presentación en modo Lista con selector de cantidad (+/-) y botón de compartir.
 */
@Composable
fun ProductoItemLista(
    producto: Producto,
    stockActual: Int,
    onClickDetalle: () -> Unit,
    onAgregarAlCarritoConCantidad: (cantidad: Int) -> Unit,
    onToggleFavorito: () -> Unit
) {
    val context = LocalContext.current
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }
    val primeraImagen = producto.imagenes.firstOrNull() ?: ""

    var cantidadDeseada by remember { mutableStateOf(1) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetalle() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CargarImagenProducto(
                uriString = primeraImagen,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Código: ${producto.codigo}  |  Precio: ${formatoMoneda.format(producto.valor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (estaAgotado) "AGOTADO (0 ${producto.unidadMedida})" else "Stock real: $stockActual ${producto.unidadMedida}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (estaAgotado) Color(0xFFC62828) else Color(0xFF2E7D32)
                )

                if (!estaAgotado) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            IconButton(
                                onClick = { if (cantidadDeseada > 1) cantidadDeseada -= 1 },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(12.dp))
                            }
                        }

                        Text(
                            text = "$cantidadDeseada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            IconButton(
                                onClick = { if (cantidadDeseada < stockActual) cantidadDeseada += 1 },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = { CompartirUtils.compartirProducto(context, producto) }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onToggleFavorito, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (esFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onAgregarAlCarritoConCantidad(cantidadDeseada) },
                    enabled = !estaAgotado,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Al Carrito",
                        tint = if (estaAgotado) Color.Gray else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
