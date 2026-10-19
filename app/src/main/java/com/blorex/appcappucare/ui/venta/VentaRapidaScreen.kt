package com.blorex.appcappucare.ui.venta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.viewmodel.VentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaRapidaScreen(
    modifier: Modifier = Modifier,
    vm: VentaViewModel = viewModel()
) {
    val productos = ProductosRepository.getProductosFicticios()
    var busqueda by remember { mutableStateOf("") }
    var cantidadInput by remember { mutableStateOf("1") }
    var productoSeleccionado by remember { mutableStateOf<com.blorex.appcappucare.model.Producto?>(null) }
    var errorStock by remember { mutableStateOf<String?>(null) }
    var ventaConfirmada by remember { mutableStateOf<String?>(null) }

    val productosFiltrados = productos.filter {
        it.nombre.contains(busqueda, true) || it.codigo.contains(busqueda, true)
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(value = busqueda, onValueChange = { busqueda = it }, label = { Text("Buscar") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {}) { Text("QR") }
        }
        Spacer(Modifier.height(8.dp))

        if (busqueda.length >= 2) {
            LazyColumn(Modifier.height(150.dp)) {
                items(productosFiltrados) { p ->
                    ListItem(headlineContent = { Text("${p.nombre} - ${p.cantidadDisponible}") }, supportingContent = { Text(p.codigo) })
                    HorizontalDivider()
                }
            }
        }

        if (productosFiltrados.isNotEmpty() && productoSeleccionado == null) {
            productoSeleccionado = productosFiltrados.first()
        }

        productoSeleccionado?.let { prod ->
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Producto: ${prod.nombre}")
                    Text("Disponible: ${prod.cantidadDisponible}", color = if (prod.estadoStock == EstadoStock.AGOTADO) Color.Red else Color.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = cantidadInput, onValueChange = { cantidadInput = it.filter { c -> c.isDigit() } }, label = { Text("Cant.") }, modifier = Modifier.width(100.dp))
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val cant = cantidadInput.toIntOrNull() ?: 0
                            if (cant > prod.cantidadDisponible) errorStock = "Solo quedan ${prod.cantidadDisponible}"
                            else if (cant <= 0) errorStock = "Cantidad inválida"
                            else {
                                vm.agregarAlCarrito(prod, cant)
                                errorStock = null
                                busqueda = ""
                                productoSeleccionado = null
                            }
                        }) { Text("Agregar") }
                    }
                    errorStock?.let { msg -> Text(msg, color = Color.Red) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Text("Carrito: ${vm.carrito.value.sumOf { it.cantidadVendida }} items", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(vm.carrito.value) { item ->
                ListItem(headlineContent = { Text("${item.producto.nombre} x ${item.cantidadVendida}") })
                HorizontalDivider()
            }
        }

        OutlinedTextField(value = vm.cliente.value, onValueChange = { vm.cliente.value = it }, label = { Text("Cliente") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (vm.carrito.value.isNotEmpty()) {
                val venta = vm.finalizarVenta()
                ventaConfirmada = venta.folio
            }
        }, Modifier.fillMaxWidth()) { Text("Finalizar Venta") }

        ventaConfirmada?.let { folio ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF6B7D3A)), modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                Text("¡Venta registrada! Folio: $folio", Modifier.padding(16.dp), color = Color.White)
            }
        }
    }
}
