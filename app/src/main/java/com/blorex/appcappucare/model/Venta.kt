package com.blorex.appcappucare.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Ítem que representa un producto y la cantidad seleccionada dentro del carrito o venta.
 *
 * @property producto Insumo médico seleccionado.
 * @property cantidadVendida Unidades a comercializar.
 */
data class VentaItem(
    val producto: Producto,
    var cantidadVendida: Int
)

/**
 * Modelo de datos que representa una transacción de venta completada en el sistema.
 *
 * @property folio Identificador o número de comprobante único generado para la venta.
 * @property fecha Fecha y hora en que se procesó la transacción.
 * @property cliente Nombre o razón social del cliente.
 * @property items Lista de productos y cantidades incluidas en la venta.
 * @property totalItems Suma total de unidades vendidas en la transacción.
 */
data class Venta(
    val folio: String = "VTA-${(1000..9999).random()}",
    val fecha: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
    val cliente: String,
    val items: List<VentaItem>,
    val totalItems: Int = items.sumOf { it.cantidadVendida }
)
