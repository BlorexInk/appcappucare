package com.blorex.appcappucare.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.FavoritosRepository
import com.blorex.appcappucare.repository.ProductosRepository
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
 * Muestra las tarjetas de los productos con filtros de ordenamiento (precio, A-Z),
 * alternancia de vista en Cuadrícula / Lista, apertura del diálogo de Detalle de Producto,
 * y validaciones en tiempo real.
 *
 * @param modifier Modificador composable de diseño.
 * @param ventaViewModel ViewModel que gestiona las transacciones y carrito de compras.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoShowcaseScreen(
    modifier: Modifier = Modifier,
    ventaViewModel: VentaViewModel
) {
    var busqueda by remember { mutableStateOf("") }
    var esVistaCuadricula by remember { mutableStateOf(true) }
    var ordenActual by remember { mutableStateOf(OrdenamientoCatalogo.ALFABETICO_AZ) }
    var mostrarMenuOrden by remember { mutableStateOf(false) }

    var productoDetalleSeleccionado by remember { mutableStateOf<Producto?>(null) }

    val todosLosProductos = ProductosRepository.getProductosFicticios()
    val snackbarHostState = remember { SnackbarHostState() }

    val productosFiltrados = todosLosProductos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) || it.codigo.contains(busqueda, ignoreCase = true)
    }

    val productosOrdenados = when (ordenActual) {
        OrdenamientoCatalogo.ALFABETICO_AZ -> productosFiltrados.sortedBy { it.nombre }
        OrdenamientoCatalogo.ALFABETICO_ZA -> productosFiltrados.sortedByDescending { it.nombre }
        OrdenamientoCatalogo.PRECIO_MAYOR_MENOR -> productosFiltrados.sortedByDescending { it.valor }
        OrdenamientoCatalogo.PRECIO_MENOR_MAYOR -> productosFiltrados.sortedBy { it.valor }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catálogo de Productos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Presiona un insumo para ver sus detalles, precio e imágenes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Catálogo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Warning Banner
            ventaViewModel.mensajeAdvertenciaStock.value?.let { advertencia ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta Stock",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = advertencia,
                            color = Color(0xFFE65100),
                            fontSize = 12.sp,
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
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Filter Button
                Box {
                    IconButton(onClick = { mostrarMenuOrden = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar")
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
                        imageVector = if (esVistaCuadricula) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Cambiar Vista"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Productos Disponibles (${productosOrdenados.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                onAgregarAlCarrito = { ventaViewModel.agregarAlCarrito(producto, 1) },
                                onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                onAgregarAlCarrito = { ventaViewModel.agregarAlCarrito(producto, 1) },
                                onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalle de Producto al presionar una tarjeta
    productoDetalleSeleccionado?.let { producto ->
        val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)
        DetalleProductoDialog(
            producto = producto,
            stockActual = stockActual,
            onDismiss = { productoDetalleSeleccionado = null },
            onAgregarAlCarrito = { ventaViewModel.agregarAlCarrito(producto, 1) },
            onToggleFavorito = { FavoritosRepository.toggleFavorito(producto.codigo) }
        )
    }
}

/**
 * Tarjeta de presentación en modo Cuadrícula (Grid) para un producto.
 */
@Composable
fun ProductoShowcaseCard(
    producto: Producto,
    stockActual: Int,
    onClickDetalle: () -> Unit,
    onAgregarAlCarrito: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetalle() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorito,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (esFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (esFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2
            )

            Text(
                text = "Precio: ${formatoMoneda.format(producto.valor)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = when {
                    estaAgotado -> Color(0xFFFFEBEE)
                    producto.estadoStock == EstadoStock.STOCK_BAJO -> Color(0xFFFFF8E1)
                    else -> Color(0xFFE8F5E9)
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (estaAgotado) "AGOTADO" else "Stock: $stockActual ${producto.unidadMedida}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            estaAgotado -> Color(0xFFC62828)
                            producto.estadoStock == EstadoStock.STOCK_BAJO -> Color(0xFFF57F17)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAgregarAlCarrito,
                enabled = !estaAgotado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (estaAgotado) "No disponible" else "Al Carrito", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Tarjeta de presentación en modo Lista para un producto.
 */
@Composable
fun ProductoItemLista(
    producto: Producto,
    stockActual: Int,
    onClickDetalle: () -> Unit,
    onAgregarAlCarrito: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetalle() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (estaAgotado) "AGOTADO (0 ${producto.unidadMedida})" else "Stock real: $stockActual ${producto.unidadMedida}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (estaAgotado) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (esFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (esFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onAgregarAlCarrito,
                    enabled = !estaAgotado
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Al Carrito",
                        tint = if (estaAgotado) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
