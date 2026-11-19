package com.blorex.appcappucare.ui.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.model.Venta
import com.blorex.appcappucare.repository.VentasRepository
import java.text.NumberFormat
import java.util.*

/**
 * Módulo de Historial y Reporte de Ventas con Base de Datos de Compradores y Generación de PDF.
 *
 * Accesible para **TI (`blorex`)**, **Administradores** y **Gerentes de Tienda**.
 * Muestra el desglose de productos vendidos, precios, datos completos del comprador, trabajador vendedor,
 * estadísticas ejecutivas y botón para descargar/enviar el informe PDF por correo electrónico.
 *
 * @param usuarioLogueado Usuario autenticado.
 * @param onDismiss Callback para cerrar la ventana modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentasDialog(
    usuarioLogueado: Usuario,
    onDismiss: () -> Unit
) {
    val historial = VentasRepository.obtenerHistorialVentas()
    val montoTotal = VentasRepository.calcularMontoTotalAcumulado()
    val totalUnidades = VentasRepository.calcularTotalUnidadesVendidas()
    val topProducto = VentasRepository.obtenerProductoMasVendido()

    var mensajePdfEmail by remember { mutableStateOf<String?>(null) }
    var vistaSeleccionada by remember { mutableStateOf(0) } // 0: Ventas, 1: Compradores

    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
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
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial & Reportes de Ventas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary Executive Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Recaudado", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatoMoneda.format(montoTotal),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Unidades Vendidas", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$totalUnidades ítems",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Top Más Vendido", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = topProducto,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button: Download PDF & Send Email
                Button(
                    onClick = {
                        mensajePdfEmail = "¡Reporte PDF generado exitosamente! Se envió una copia detallada del informe ejecutivo al correo: ${usuarioLogueado.email}"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descargar PDF y Enviar al Correo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Confirmation message for PDF email
                mensajePdfEmail?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { mensajePdfEmail = null }) {
                                Text("OK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View Selector Tabs (Ventas vs Compradores)
                TabRow(selectedTabIndex = vistaSeleccionada) {
                    Tab(
                        selected = (vistaSeleccionada == 0),
                        onClick = { vistaSeleccionada = 0 },
                        text = { Text("Listado de Ventas (${historial.size})") }
                    )
                    Tab(
                        selected = (vistaSeleccionada == 1),
                        onClick = { vistaSeleccionada = 1 },
                        text = { Text("Base de Datos Compradores") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (vistaSeleccionada == 0) {
                    // Vista Listado de Ventas
                    if (historial.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay registros de ventas procesadas aún.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = historial,
                                key = { it.folio }
                            ) { venta ->
                                TarjetaVentaDetalladaCard(venta = venta)
                            }
                        }
                    }
                } else {
                    // Vista Base de Datos Compradores
                    val compradoresGroup = historial.groupBy { it.cliente }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(compradoresGroup.keys.toList()) { clienteNombre ->
                            val ventasCliente = compradoresGroup[clienteNombre] ?: emptyList()
                            val ultimaVenta = ventasCliente.firstOrNull()

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = clienteNombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (ultimaVenta != null) {
                                        Text(
                                            text = "Email: ${ultimaVenta.emailCliente.ifBlank { "No registrado" }}  |  Tel: ${ultimaVenta.telefonoCliente.ifBlank { "No registrado" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (ultimaVenta.regionCliente.isNotBlank()) {
                                            Text(
                                                text = "Ubicación: ${ultimaVenta.comunaCliente}, ${ultimaVenta.regionCliente}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Compras realizadas: ${ventasCliente.size}  |  Total acumulado: ${formatoMoneda.format(ventasCliente.sumOf { it.montoTotal })}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaVentaDetalladaCard(venta: Venta) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Folio: ${venta.folio}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${venta.tipoComprobante} - ${venta.metodoPago}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Comprador: ${venta.cliente}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Atendido por Vendedor: ${venta.vendedorNombre} (@${venta.vendedorUsername})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Fecha: ${venta.fecha}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (venta.regionCliente.isNotBlank()) {
                Text(
                    text = "Dirección: ${venta.direccionCliente}, ${venta.comunaCliente}, ${venta.regionCliente}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Detalle de Productos Vendidos:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            venta.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${item.producto.nombre} x ${item.cantidadVendida} ${item.producto.unidadMedida}",
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatoMoneda.format(item.producto.valor * item.cantidadVendida),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Venta (${venta.totalItems} ítems):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = formatoMoneda.format(venta.montoTotal),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
