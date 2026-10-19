package com.blorex.appcappucare.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
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

/**
 * Pantalla del Catálogo de Productos (Showcase).
 *
 * Muestra las tarjetas promocionales de los productos disponibles con control de favoritos (Corazón),
 * botón para agregar al carrito y validaciones en tiempo real de stock de inventario.
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
    val todosLosProductos = ProductosRepository.getProductosFicticios()
    val snackbarHostState = remember { SnackbarHostState() }

    val productosDisponibles = todosLosProductos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) || it.codigo.contains(busqueda, ignoreCase = true)
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
                            text = "Guarda tus insumos con el corazón o agrégalos al carrito",
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

            // Search Bar
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar insumo en catálogo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Productos en Catálogo (${productosDisponibles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (productosDisponibles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos disponibles con ese criterio de búsqueda.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = productosDisponibles,
                        key = { it.codigo }
                    ) { producto ->
                        val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)

                        ProductoShowcaseCard(
                            producto = producto,
                            stockActual = stockActual,
                            onAgregarAlCarrito = {
                                ventaViewModel.agregarAlCarrito(producto, 1)
                            },
                            onToggleFavorito = {
                                FavoritosRepository.toggleFavorito(producto.codigo)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de presentación individual para cada producto del catálogo.
 *
 * @param producto Insumo a mostrar.
 * @param stockActual Disponibilidad actual en sistema.
 * @param onAgregarAlCarrito Callback ejecutado al presionar "Al Carrito".
 * @param onToggleFavorito Callback para marcar o desmarcar el corazón de favorito.
 */
@Composable
fun ProductoShowcaseCard(
    producto: Producto,
    stockActual: Int,
    onAgregarAlCarrito: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Favorite Heart Icon
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

                // Heart Button
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
                text = producto.codigo,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

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
                        text = if (estaAgotado) "AGOTADO (0 ${producto.unidadMedida})" else "Stock: $stockActual ${producto.unidadMedida}",
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
