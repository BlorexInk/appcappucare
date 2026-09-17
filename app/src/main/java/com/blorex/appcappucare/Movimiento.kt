package com.blorex.appcappucare

data class Movimiento(
    val id: String = "",
    val productoId: String = "",
    val productoNombre: String = "",
    val fecha: String = "",
    val tipo: String = "", // "Ingreso" o "Salida"
    val cantidadInvolucrada: Int = 0,
    val motivo: String = "", // "Venta", "Reposición", etc.
    val responsable: String = ""
)