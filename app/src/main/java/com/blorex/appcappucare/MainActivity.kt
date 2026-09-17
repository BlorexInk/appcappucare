package com.blorex.appcappucare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.blorex.appcappucare.ui.theme.AppcappucareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppcappucareTheme {
                // Contenedor principal que usa el color de fondo del sistema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llamamos a nuestra pantalla de inicio
                    DashboardScreen(
                        onNavigateToInventario = {
                            // Aquí programaremos el viaje al catálogo en la Fase 2
                        },
                        onNavigateToMovimientos = {
                            // Aquí programaremos el viaje a entradas/salidas en la Fase 3
                        }
                    )
                }
            }
        }
    }
}
