package com.blorex.appcappucare.ui.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.repository.api.AsistenteIARepository
import com.blorex.appcappucare.repository.api.MensajeIA
import com.blorex.appcappucare.repository.api.ProductoSugerido
import com.blorex.appcappucare.repository.api.ResultadoIA
import com.blorex.appcappucare.repository.sync.SyncBackendRepository
import com.blorex.appcappucare.viewmodel.VentaViewModel
import kotlinx.coroutines.launch
import java.util.Locale

private val SUGERENCIAS = listOf(
    "Kit de curación básico",
    "Kit para inyectables",
    "Equivalentes a la mascarilla KN95",
    "Resume la ficha del termómetro digital"
)

private fun precio(valor: Double): String =
    "$" + String.format(Locale("es", "CL"), "%,.0f", valor)

/**
 * Chat de compras con IA: busca equivalentes, arma listas por especialidad
 * y resume fichas. Solo recomienda productos del catálogo de la app.
 */
@Composable
fun AsistenteIADialog(
    ventaViewModel: VentaViewModel,
    onCerrar: () -> Unit
) {
    val mensajes = remember { mutableStateListOf<MensajeIA>() }
    var entrada by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun enviar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty() || cargando) return
        val historial = mensajes.toList()
        mensajes.add(MensajeIA(esUsuario = true, texto = limpio))
        entrada = ""
        aviso = null
        cargando = true
        scope.launch {
            val baseUrl = SyncBackendRepository.servidorUrlInput.trim().removeSuffix("/")
            val catalogo = ProductosRepository.getProductosFicticios().toList()
            when (val r = AsistenteIARepository.preguntar(baseUrl, limpio, historial, catalogo)) {
                is ResultadoIA.Exito ->
                    mensajes.add(MensajeIA(false, r.respuesta.texto, r.respuesta.sugeridos))
                is ResultadoIA.Error ->
                    mensajes.add(MensajeIA(false, r.mensaje))
            }
            cargando = false
        }
    }

    // Baja automáticamente al último mensaje
    LaunchedEffect(mensajes.size, cargando) {
        val total = 1 + mensajes.size + (if (cargando) 1 else 0)
        listState.animateScrollToItem(total - 1)
    }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Encabezado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Asistente de compras IA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Solo recomienda productos de este catálogo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Conversación
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Burbuja(
                            texto = "Hola, soy tu asistente. Puedo buscar productos equivalentes, " +
                                "armar listas de compra por especialidad (por ejemplo un kit de curación) " +
                                "y resumir la ficha de un producto. No entrego indicaciones clínicas: " +
                                "eso lo define un profesional de salud.",
                            esUsuario = false
                        )
                    }
                    items(mensajes) { m ->
                        Column {
                            Burbuja(texto = m.texto, esUsuario = m.esUsuario)
                            m.sugeridos.forEach { s ->
                                Spacer(modifier = Modifier.height(4.dp))
                                TarjetaSugerido(
                                    s = s,
                                    onAgregar = {
                                        val prod = ProductosRepository.getProductosFicticios()
                                            .find { it.codigo.equals(s.codigo, ignoreCase = true) }
                                        aviso = if (prod == null) {
                                            "No encontré ese producto en el catálogo."
                                        } else if (ventaViewModel.agregarAlCarrito(prod, s.cantidad)) {
                                            "Agregado al carrito: ${prod.nombre} x${s.cantidad}"
                                        } else {
                                            "No se pudo agregar: revisa el stock disponible."
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (cargando) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pensando...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                aviso?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sugerencias rápidas
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(SUGERENCIAS) { texto ->
                        AssistChip(onClick = { enviar(texto) }, label = { Text(texto) })
                    }
                }

                // Entrada de texto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = entrada,
                        onValueChange = { if (it.length <= 600) entrada = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe tu consulta...") },
                        maxLines = 3
                    )
                    IconButton(
                        onClick = { enviar(entrada) },
                        enabled = entrada.isNotBlank() && !cargando
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
private fun Burbuja(texto: String, esUsuario: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (esUsuario) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (esUsuario) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                texto,
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (esUsuario) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaSugerido(s: ProductoSugerido, onAgregar: () -> Unit) {
    val agotado = s.estadoStock == "AGOTADO"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(s.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${s.codigo} · ${precio(s.valor)} · Cantidad: ${s.cantidad}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (agotado) {
                    Text(
                        "Agotado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Button(onClick = onAgregar, enabled = !agotado) { Text("Agregar") }
        }
    }
}
