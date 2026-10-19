package com.blorex.appcappucare.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.ProductosRepository

@Composable
fun DashboardNotificacionesScreen(
    modifier: Modifier = Modifier
) {
    val todosLosProductos = ProductosRepository.getProductosFicticios()

    val productosAgotados = todosLosProductos.filter { it.estadoStock == EstadoStock.AGOTADO }
    val productosStockBajo = todosLosProductos.filter { it.estadoStock == EstadoStock.STOCK_BAJO }
    val productosDisponibles = todosLosProductos.filter { it.estadoStock == EstadoStock.DISPONIBLE }

    val totalAlertas = productosAgotados.size + productosStockBajo.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title & Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Alertas",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Centro de Notificaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Alertas de stock en tiempo real",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricaCard(
                titulo = "Agotados",
                valor = productosAgotados.size.toString(),
                colorFondo = Color(0xFFFFEBEE),
                colorTexto = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            MetricaCard(
                titulo = "Stock Bajo",
                valor = productosStockBajo.size.toString(),
                colorFondo = Color(0xFFFFF8E1),
                colorTexto = Color(0xFFF57F17),
                modifier = Modifier.weight(1f)
            )
            MetricaCard(
                titulo = "Disponibles",
                valor = productosDisponibles.size.toString(),
                colorFondo = Color(0xFFE8F5E9),
                colorTexto = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Alertas Recientes ($totalAlertas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (totalAlertas == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ok",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Excelente! No hay productos agotados ni con stock bajo.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First show Agotados (Critical)
                items(productosAgotados) { producto ->
                    NotificacionCard(
                        producto = producto,
                        esAgotado = true
                    )
                }

                // Next show Stock Bajo (Warning)
                items(productosStockBajo) { producto ->
                    NotificacionCard(
                        producto = producto,
                        esAgotado = false
                    )
                }
            }
        }
    }
}

@Composable
fun MetricaCard(
    titulo: String,
    valor: String,
    colorFondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
            Text(
                text = titulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colorTexto
            )
        }
    }
}

@Composable
fun NotificacionCard(
    producto: Producto,
    esAgotado: Boolean
) {
    val containerColor = if (esAgotado) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
    val iconColor = if (esAgotado) Color(0xFFC62828) else Color(0xFFF57F17)
    val titleColor = if (esAgotado) Color(0xFFB71C1C) else Color(0xFFE65100)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (esAgotado) Icons.Default.Error else Icons.Default.Warning,
                contentDescription = if (esAgotado) "Agotado" else "Stock Bajo",
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (esAgotado) "¡PRODUCTO AGOTADO!" else "ALERTA: STOCK BAJO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = titleColor
                )

                Text(
                    text = "${producto.nombre} (${producto.codigo})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (esAgotado)
                        "Sin existencias. Requiere reabastecimiento urgente de la unidad ${producto.unidadMedida}."
                    else
                        "Quedan solo ${producto.cantidadDisponible} ${producto.unidadMedida} (Mínimo recomendado: ${producto.nivelMinimo}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
