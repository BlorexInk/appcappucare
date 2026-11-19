package com.blorex.appcappucare.ui.carrito

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Datos del comprador, facturación y despacho capturados en el formulario de checkout.
 */
data class DatosCheckout(
    val nombreComprador: String,
    val telefono: String,
    val email: String,
    val tipoComprobante: String, // "Boleta" o "Factura"
    val rutEmpresa: String = "",
    val razonSocial: String = "",
    val esEnvioDomicilio: Boolean = false,
    val regionChile: String = "",
    val comunaChile: String = "",
    val direccionEnvio: String = "",
    val metodoPago: String = "Webpay / Transbank" // "Webpay / Transbank", "Transferencia Bancaria", "Efectivo"
)

/** Map de Regiones de Chile y sus principales Comunas. */
val RegionesChileMap = mapOf(
    "Región Metropolitana de Santiago" to listOf("Santiago", "Providencia", "Las Condes", "Maipú", "Ñuñoa", "La Florida", "Puente Alto", "San Bernardo"),
    "Valparaíso" to listOf("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "San Antonio"),
    "Biobío" to listOf("Concepción", "Talcahuano", "Los Ángeles", "Chiguayante", "San Pedro de la Paz"),
    "Antofagasta" to listOf("Antofagasta", "Calama", "Tocopilla"),
    "La Araucanía" to listOf("Temuco", "Padre Las Casas", "Villarrica", "Pucón"),
    "Coquimbo" to listOf("La Serena", "Coquimbo", "Ovalle"),
    "Maule" to listOf("Talca", "Curicó", "Linares"),
    "Los Lagos" to listOf("Puerto Montt", "Osorno", "Castro"),
    "Tarapacá" to listOf("Iquique", "Alto Hospicio"),
    "O'Higgins" to listOf("Rancagua", "San Fernando"),
    "Ñuble" to listOf("Chillán", "San Carlos"),
    "Arica y Parinacota" to listOf("Arica"),
    "Atacama" to listOf("Copiapó", "Vallenar"),
    "Los Ríos" to listOf("Valdivia", "La Unión"),
    "Aysén" to listOf("Coyhaique"),
    "Magallanes y Antártica Chilena" to listOf("Punta Arenas")
)

/**
 * Formulario de Datos del Comprador, Comprobante (Boleta/Factura), Despacho para Chile y Métodos de Pago (Webpay).
 *
 * @param onDismiss Callback para cerrar la ventana modal.
 * @param onConfirmarCheckout Callback con los [DatosCheckout] confirmados para procesar la compra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCheckoutDialog(
    onDismiss: () -> Unit,
    onConfirmarCheckout: (DatosCheckout) -> Unit
) {
    var nombreComprador by remember { mutableStateOf("Carlos Blorex") }
    var telefono by remember { mutableStateOf("+56 9 1234 5678") }
    var email by remember { mutableStateOf("carlos@appcappucare.com") }

    var tipoComprobante by remember { mutableStateOf("Boleta") }
    var rutEmpresa by remember { mutableStateOf("76.123.456-7") }
    var razonSocial by remember { mutableStateOf("Insumos Médicos SpA") }

    var esEnvioDomicilio by remember { mutableStateOf(false) }
    var regionSeleccionada by remember { mutableStateOf("Región Metropolitana de Santiago") }
    var comunaSeleccionada by remember { mutableStateOf("Santiago") }
    var direccionEnvio by remember { mutableStateOf("Av. Providencia 1234") }

    var metodoPago by remember { mutableStateOf("Webpay / Transbank") }

    var expandirRegionDropdown by remember { mutableStateOf(false) }
    var expandirComunaDropdown by remember { mutableStateOf(false) }

    val comunasDisponibles = RegionesChileMap[regionSeleccionada] ?: emptyList()

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Datos de Pago y Despacho",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Datos del Comprador
                Text("1. Información del Comprador / Empresa", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = nombreComprador,
                    onValueChange = { nombreComprador = it },
                    label = { Text("Nombre o Razón Social") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono (+56)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // 2. Tipo de Comprobante
                Text("2. Tipo de Comprobante de Venta", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (tipoComprobante == "Boleta"), onClick = { tipoComprobante = "Boleta" })
                    Text("Boleta Electrónica", modifier = Modifier.clickable { tipoComprobante = "Boleta" })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = (tipoComprobante == "Factura"), onClick = { tipoComprobante = "Factura" })
                    Text("Factura Electrónica", modifier = Modifier.clickable { tipoComprobante = "Factura" })
                }

                if (tipoComprobante == "Factura") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rutEmpresa,
                            onValueChange = { rutEmpresa = it },
                            label = { Text("RUT Empresa") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = razonSocial,
                            onValueChange = { razonSocial = it },
                            label = { Text("Razón Social / Giro") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // 3. Opción de Envío a Domicilio (Regiones y Comunas de Chile)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3. Envío a Domicilio (Chile)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Switch(
                        checked = esEnvioDomicilio,
                        onCheckedChange = { esEnvioDomicilio = it }
                    )
                }

                if (esEnvioDomicilio) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de Regiones de Chile
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = regionSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Región de Chile") },
                            trailingIcon = {
                                IconButton(onClick = { expandirRegionDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandirRegionDropdown = true }
                        )

                        DropdownMenu(
                            expanded = expandirRegionDropdown,
                            onDismissRequest = { expandirRegionDropdown = false }
                        ) {
                            RegionesChileMap.keys.forEach { reg ->
                                DropdownMenuItem(
                                    text = { Text(reg) },
                                    onClick = {
                                        regionSeleccionada = reg
                                        comunaSeleccionada = RegionesChileMap[reg]?.firstOrNull() ?: ""
                                        expandirRegionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de Comuna
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = comunaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Comuna") },
                            trailingIcon = {
                                IconButton(onClick = { expandirComunaDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandirComunaDropdown = true }
                        )

                        DropdownMenu(
                            expanded = expandirComunaDropdown,
                            onDismissRequest = { expandirComunaDropdown = false }
                        ) {
                            comunasDisponibles.forEach { com ->
                                DropdownMenuItem(
                                    text = { Text(com) },
                                    onClick = {
                                        comunaSeleccionada = com
                                        expandirComunaDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = direccionEnvio,
                        onValueChange = { direccionEnvio = it },
                        label = { Text("Dirección de Entrega / Calle y Número") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // 4. Métodos de Pago
                Text("4. Método de Pago (Pasarela de Pago)", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                val metodos = listOf("Webpay / Transbank", "Transferencia Bancaria", "Efectivo / Mostrador")
                metodos.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { metodoPago = m }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = (metodoPago == m), onClick = { metodoPago = m })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(m, fontSize = 13.sp, fontWeight = if (metodoPago == m) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val datos = DatosCheckout(
                            nombreComprador = nombreComprador.ifBlank { "Venta Mostrador" },
                            telefono = telefono,
                            email = email,
                            tipoComprobante = tipoComprobante,
                            rutEmpresa = if (tipoComprobante == "Factura") rutEmpresa else "",
                            razonSocial = if (tipoComprobante == "Factura") razonSocial else "",
                            esEnvioDomicilio = esEnvioDomicilio,
                            regionChile = if (esEnvioDomicilio) regionSeleccionada else "",
                            comunaChile = if (esEnvioDomicilio) comunaSeleccionada else "",
                            direccionEnvio = if (esEnvioDomicilio) direccionEnvio else "",
                            metodoPago = metodoPago
                        )
                        onConfirmarCheckout(datos)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar y Pagar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
