package com.blorex.appcappucare.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MedicalServices
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
import java.text.NumberFormat
import java.util.*

/**
 * Diálogo / Modal de Detalle de Producto.
 *
 * Muestra la información completa de un insumo médico seleccionado del catálogo:
 * galería de hasta 4 imágenes, precio ($ Valor), código, stock en tiempo real,
 * unidad de medida, descripción detallada y botones de "Agregar al Carrito" y "Favorito".
 *
 * @param producto Insumo médico a detallar.
 * @param stockActual Disponibilidad actual en sistema.
 * @param onDismiss Callback para cerrar la ventana modal.
 * @param onAgregarAlCarrito Callback para agregar 1 unidad al carrito de compras.
 * @param onToggleFavorito Callback para alternar el estado de favorito (Corazón).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoDialog(
    producto: Producto,
    stockActual: Int,
    onDismiss: () -> Unit,
    onAgregarAlCarrito: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val esFav = FavoritosRepository.esFavorito(producto.codigo)
    val estaAgotado = stockActual <= 0
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    var imagenSeleccionadaIndex by remember { mutableStateOf(0) }

    val imagenesGaleria = if (producto.imagenes.isEmpty()) {
        listOf("Imagen Principal", "Vista Posterior", "Empaque", "Detalle Ficha")
    } else {
        producto.imagenes.take(4)
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
                    .padding(20.dp)
            ) {
                // Header with Close and Favorite Heart Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }

                    IconButton(onClick = onToggleFavorito) {
                        Icon(
                            imageVector = if (esFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Image Display Placeholder / Card
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Imagen Producto",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = imagenesGaleria.getOrElse(imagenSeleccionadaIndex) { "Imagen de Muestra" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gallery Thumbnails (Up to 4 Images)
                Text(
                    text = "Galería de Imágenes (${imagenesGaleria.size}/4)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(imagenesGaleria) { index, imgName ->
                        Surface(
                            color = if (index == imagenSeleccionadaIndex) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            onClick = { imagenSeleccionadaIndex = index },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Img ${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == imagenSeleccionadaIndex) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Title & Price
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio: ${formatoMoneda.format(producto.valor)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = if (estaAgotado) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (estaAgotado) Color(0xFFC62828) else Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (estaAgotado) "AGOTADO" else "Stock: $stockActual ${producto.unidadMedida}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (estaAgotado) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Código Identificador: ${producto.codigo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Full Description
                Text(
                    text = "Descripción del Insumo Médico:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (producto.descripcion.isBlank())
                        "Insumo médico hospitalario de alta calidad para procedimientos clínicos y de salud."
                    else producto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: Add to Cart
                Button(
                    onClick = {
                        onAgregarAlCarrito()
                        onDismiss()
                    },
                    enabled = !estaAgotado,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Al Carrito",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (estaAgotado) "Producto No Disponible" else "Agregar al Carrito",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
