package com.blorex.appcappucare.model

enum class EstadoStock {
    DISPONIBLE,
    STOCK_BAJO,
    AGOTADO
}

data class Producto(
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val valor: Double = 0.0,
    val cantidadDisponible: Int,
    val unidadMedida: String,
    val nivelMinimo: Int,
    val estadoStock: EstadoStock,
    val imagenes: List<String> = emptyList(),
    val fotoUri: String? = null
)