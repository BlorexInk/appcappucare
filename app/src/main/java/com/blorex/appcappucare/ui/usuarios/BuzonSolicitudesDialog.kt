package com.blorex.appcappucare.ui.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blorex.appcappucare.model.SolicitudModificacion
import com.blorex.appcappucare.repository.SolicitudesRepository

/**
 * Buzón de Solicitudes de Modificación de Datos de Trabajadores.
 *
 * Módulo exclusivo para el usuario de **TI (`blorex`)** que recibe las notificaciones de requerimientos
 * enviadas por los empleados y permite marcarlas como atendidas/procesadas.
 *
 * @param onDismiss Callback para cerrar la ventana modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzonSolicitudesDialog(
    onDismiss: () -> Unit
) {
    val solicitudes = SolicitudesRepository.obtenerSolicitudes()

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buzón de Solicitudes (TI)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "Requerimientos de cambio de datos enviados por trabajadores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (solicitudes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay solicitudes de modificación recibidas.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = solicitudes,
                            key = { it.id }
                        ) { sol ->
                            TarjetaSolicitudCard(
                                solicitud = sol,
                                onMarcarAtendida = {
                                    SolicitudesRepository.marcarComoAtendido(sol.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaSolicitudCard(
    solicitud: SolicitudModificacion,
    onMarcarAtendida: () -> Unit
) {
    val esPendiente = solicitud.estado.equals("PENDIENTE", ignoreCase = true)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (esPendiente) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (esPendiente) Color(0xFFFFE0B2) else Color(0xFFC8E6C9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (esPendiente) Icons.Default.PendingActions else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (esPendiente) Color(0xFFE65100) else Color(0xFF2E7D32),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = solicitud.estado.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esPendiente) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }
                }

                Text(
                    text = solicitud.fechaSolicitud,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = solicitud.asunto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Empleado: ${solicitud.nombreEmpleado} (@${solicitud.usernameEmpleado})  |  ${solicitud.rolEmpleado}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = solicitud.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (esPendiente) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onMarcarAtendida,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Marcar como Atendida", fontSize = 12.sp)
                }
            }
        }
    }
}
