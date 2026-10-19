package com.blorex.appcappucare.ui.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.VentaItem
import com.blorex.appcappucare.viewmodel.VentaViewModel

/**
 * Pantalla del Carrito de Compras.
 *
 * Muestra la lista de insumos agregados con validación de stock en tiempo real.
 * Si un producto se agota o supera la disponibilidad en sistema, bloquea la compra e indica advertencias.
 *
 * @param modifier Modificador composable de diseño.
 * @param ventaViewModel ViewModel del carrito y pagos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    modifier: Modifier = Modifier,
    ventaViewModel: VentaViewModel
) {
    val itemsCarrito = ventaViewModel.carrito.value
    val totalItems = itemsCarrito.sumOf { it.cantidadVendida }
    val scope = rememberCoroutineScope()
    val procesandoPago = ventaViewModel.procesandoPago

    Column(
        modifier = modifier
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
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Carrito de Compras",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Revisa y gestiona tus insumos con validación de stock en tiempo real",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading Overlay Card during payment simulation (3 to 5 seconds)
        if (procesandoPago) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Procesando pago de la compra...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Por favor espera un momento mientras confirmamos...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Warning Banner for Stock Limit Exceeded or Out of Stock Item
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
                        contentDescription = "Advertencia de Stock",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = advertencia,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { ventaViewModel.limpiarAdvertenciaStock() }) {
                        Text("Cerrar", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Success Payment Confirmation Card
        ventaViewModel.mensajeConfirmacionPago.value?.let { mensaje ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mensaje,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { ventaViewModel.limpiarMensaje() }) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = "Productos en Carrito ($totalItems ítems)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (itemsCarrito.isEmpty() && !procesandoPago) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Vacío",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu carrito de compras está vacío.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Agrega productos desde el catálogo o tus favoritos.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = itemsCarrito,
                    key = { it.producto.codigo }
                ) { item ->
                    val stockRealEnSistema = ventaViewModel.obtenerStockActual(item.producto.codigo)

                    CarritoItemCard(
                        item = item,
                        stockRealEnSistema = stockRealEnSistema,
                        enabled = !procesandoPago,
                        onIncrementar = { ventaViewModel.incrementarCantidad(item.producto) },
                        onDecrementar = { ventaViewModel.decrementarCantidad(item.producto) },
                        onEliminar = { ventaViewModel.eliminarDelCarrito(item.producto) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Customer Name Field
            OutlinedTextField(
                value = ventaViewModel.cliente.value,
                onValueChange = { ventaViewModel.cliente.value = it },
                label = { Text("Cliente / Destino de la Venta") },
                enabled = !procesandoPago,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pay Button with Interactive Loading Trigger
            Button(
                onClick = { ventaViewModel.pagarProductosConCarga(scope) },
                enabled = itemsCarrito.isNotEmpty() && !procesandoPago,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (procesandoPago) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando pago...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Pagar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pagar Productos ($totalItems ítems)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de producto individual dentro de la lista del carrito con validación de stock en tiempo real.
 *
 * @param item Ítem del carrito.
 * @param stockRealEnSistema Cantidad existente actual en el inventario.
 * @param enabled Estado habilitado para interacciones.
 * @param onIncrementar Callback para sumar 1 unidad.
 * @param onDecrementar Callback para restar 1 unidad.
 * @param onEliminar Callback para eliminar el ítem.
 */
@Composable
fun CarritoItemCard(
    item: VentaItem,
    stockRealEnSistema: Int,
    enabled: Boolean = true,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onEliminar: () -> Unit
) {
    val estaAgotado = stockRealEnSistema <= 0
    val excedeStock = item.cantidadVendida > stockRealEnSistema
    val alcanzolimite = item.cantidadVendida >= stockRealEnSistema

    val containerColor = when {
        estaAgotado -> Color(0xFFFFEBEE)
        excedeStock -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (estaAgotado) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Código: ${item.producto.codigo}  |  Stock real: $stockRealEnSistema ${item.producto.unidadMedida}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        IconButton(
                            onClick = onDecrementar,
                            enabled = enabled,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Restar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${item.cantidadVendida}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (excedeStock) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Surface(
                        color = if (alcanzolimite) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        IconButton(
                            onClick = onIncrementar,
                            enabled = enabled && !alcanzolimite && !estaAgotado,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sumar",
                                tint = if (alcanzolimite || estaAgotado) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Button
                    IconButton(
                        onClick = onEliminar,
                        enabled = enabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar del Carrito",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Real-Time Warning Badges
            if (estaAgotado) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFFCDD2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFB71C1C),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Producto sin stock disponible para su venta. Favor de eliminarlo.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            } else if (excedeStock) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFFFE0B2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Excede el stock disponible en sistema ($stockRealEnSistema ${item.producto.unidadMedida}).",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            } else if (alcanzolimite) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Límite disponible alcanzado ($stockRealEnSistema ${item.producto.unidadMedida})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
