package com.blorex.appcappucare.model

/**
 * Representa los posibles estados de disponibilidad en inventario para un producto.
 */
enum class EstadoStock {
    /** Producto con cantidad disponible suficiente (mayor al nivel mínimo). */
    DISPONIBLE,

    /** Producto con cantidad disponible igual o inferior al nivel mínimo de seguridad. */
    STOCK_BAJO,

    /** Producto sin existencias disponibles (cantidad <= 0). */
    AGOTADO
}

/**
 * Modelo de datos que representa un insumo médico o producto del catálogo de inventario.
 *
 * @property codigo Código único de identificación del producto (ej: "JR-10ML").
 * @property nombre Nombre descriptivo del producto.
 * @property cantidadDisponible Cantidad actual existente en inventario.
 * @property unidadMedida Unidad de empaque o medida (ej: "un", "caja", "frasco", "paq").
 * @property nivelMinimo Umbral mínimo recomendado antes de generar alerta de stock bajo.
 * @property estadoStock Estado actual del stock (DISPONIBLE, STOCK_BAJO, AGOTADO).
 */
data class Producto(
    val codigo: String,
    val nombre: String,
    val cantidadDisponible: Int,
    val unidadMedida: String,
    val nivelMinimo: Int,
    val estadoStock: EstadoStock
)
