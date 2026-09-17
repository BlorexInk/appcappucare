package com.blorex.appcappucare

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val cantidadDisponible: Int = 0,
    val unidadMedida: String = "",
    val nivelMinimo: Int = 0, // Stock mínimo para alertas
    val ubicacion: String = "",
    val estado: String = "Disponible"
)