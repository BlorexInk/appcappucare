package com.blorex.appcappucare.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Registra un movimiento o transacción de inventario (Entrada o Salida de productos).
 *
 * @property id Identificador único del registro (UUID).
 * @property codigoProducto Código del producto involucrado.
 * @property nombreProducto Nombre del producto involucrado.
 * @property fecha Fecha y hora del registro en formato legible.
 * @property tipo Tipo de movimiento ("ENTRADA" o "SALIDA").
 * @property cantidad Cantidad ingresada o descontada.
 * @property motivo Razón del movimiento (ej: "Venta Pagada", "Ajuste de inventario").
 * @property cliente Cliente asociado a la transacción (opcional).
 */
data class Movimiento(
    val id: String = UUID.randomUUID().toString(),
    val codigoProducto: String,
    val nombreProducto: String,
    val fecha: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
    val tipo: String,
    val cantidad: Int,
    val motivo: String,
    val cliente: String? = null
)
