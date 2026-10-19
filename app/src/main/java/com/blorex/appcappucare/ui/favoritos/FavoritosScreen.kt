package com.blorex.appcappucare.ui.favoritos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.FavoritosRepository
import com.blorex.appcappucare.viewmodel.VentaViewModel

/**
 * Pantalla de Productos Guardados / Favoritos.
 *
 * Muestra la lista de insumos marcados con el corazón y permite agregarlos directamente
 * al carrito de compras con validación estricta de límites de stock.
 *
 * @param modifier Modificador composable de diseño.
 * @param ventaViewModel ViewModel que administra las compras y el carrito.
 */
@Composable
fun FavoritosScreen(
    modifier: Modifier = Modifier,
    ventaViewModel: VentaViewModel
) {
    val productosFavoritos = FavoritosRepository.obtenerProductosFavoritos()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favoritos",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Productos Guardados / Favoritos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Tus insumos marcados con el corazón",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
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

        Text(
            text = "Lista de Guardados (${productosFavoritos.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (productosFavoritos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Vacío",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes productos guardados en favoritos.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Presiona el corazón en el catálogo para agregarlos aquí.",
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
                    items = productosFavoritos,
                    key = { it.codigo }
                ) { producto ->
                    val stockActual = ventaViewModel.obtenerStockActual(producto.codigo)

                    FavoritoItemCard(
                        producto = producto,
                        stockActual = stockActual,
                        onAgregarAlCarrito = {
                            ventaViewModel.agregarAlCarrito(producto, 1)
                        },
                        onEliminarDeFavoritos = {
                            FavoritosRepository.eliminarFavorito(producto.codigo)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta individual de un producto guardado en favoritos.
 *
 * @param producto Insumo mostrado.
 * @param stockActual Cantidad disponible real en sistema.
 * @param onAgregarAlCarrito Callback para añadir 1 unidad al carrito.
 * @param onEliminarDeFavoritos Callback para eliminar de la lista de favoritos.
 */
@Composable
fun FavoritoItemCard(
    producto: Producto,
    stockActual: Int,
    onAgregarAlCarrito: () -> Unit,
    onEliminarDeFavoritos: () -> Unit
) {
    val estaAgotado = stockActual <= 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Código: ${producto.codigo}  |  Unidad: ${producto.unidadMedida}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (estaAgotado) "¡AGOTADO! No disponible para su venta" else "Disponible: $stockActual ${producto.unidadMedida}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (estaAgotado) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Button to Add to Cart
                Button(
                    onClick = onAgregarAlCarrito,
                    enabled = !estaAgotado,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Agregar al Carrito",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (estaAgotado) "Agotado" else "Al Carrito", fontSize = 12.sp)
                }

                // Button to Remove from Favorites
                OutlinedButton(
                    onClick = onEliminarDeFavoritos,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE91E63)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Quitar de Favoritos",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quitar", fontSize = 11.sp)
                }
            }
        }
    }
}
