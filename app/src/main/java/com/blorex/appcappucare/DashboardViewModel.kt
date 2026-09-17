package com.blorex.appcappucare

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    // Estados que la vista (Screen) va a estar observando
    private val _totalProductos = MutableStateFlow(0)
    val totalProductos: StateFlow<Int> = _totalProductos

    private val _productosStockBajo = MutableStateFlow(0)
    val productosStockBajo: StateFlow<Int> = _productosStockBajo

    init {
        // Aquí conectaremos Firebase más adelante para cargar los números reales
        cargarResumenInventario()
    }

    private fun cargarResumenInventario() {
        // Datos ficticios iniciales para probar que la interfaz responda al ViewModel
        _totalProductos.value = 0
        _productosStockBajo.value = 0
    }
}
