package com.blorex.appcappucare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(
    onNavigateToInventario: () -> Unit,
    onNavigateToMovimientos: () -> Unit,
    viewModel: DashboardViewModel = viewModel() // Inyección del ViewModel
) {
    // Observamos los estados del ViewModel usando MVVM
    val totalProductos by viewModel.totalProductos.collectAsState()
    val productosStockBajo by viewModel.productosStockBajo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cappucare Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Tarjeta de Estado General
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Resumen de Existencias", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Mostramos la variable reactiva del ViewModel
                Text(text = "Productos registrados: $totalProductos", fontSize = 16.sp)
            }
        }

        // Tarjeta de Alerta (Requerimiento Crítico del PDF)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ Alertas de Reposición",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFB71C1C)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Mostramos la alerta reactiva del ViewModel
                Text(
                    text = "Productos bajo el nivel mínimo: $productosStockBajo",
                    fontSize = 16.sp,
                    color = Color(0xFFB71C1C)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToInventario,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Ver Catálogo de Productos")
        }

        Button(
            onClick = onNavigateToMovimientos,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Registrar Entrada / Salida")
        }
    }
}
