package com.blorex.appcappucare.repository

import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.model.Venta
import com.blorex.appcappucare.model.VentaItem

/**
 * Repositorio de historial de ventas, compradores y generación de informes ejecutivos con estadísticas.
 */
object VentasRepository {

    private val historialVentas = mutableStateListOf<Venta>()

    init {
        // Ventas de muestra precargadas para informes y estadísticas
        val prod1 = Producto("JR-10ML", "Jeringa 10ml", "Jeringa estéril", 1200.0, 45, "un", 20, EstadoStock.DISPONIBLE)
        val prod2 = Producto("OXI-PUL-01", "Oxímetro de Pulso", "Oxímetro digital", 24900.0, 12, "un", 5, EstadoStock.DISPONIBLE)
        val prod3 = Producto("TERM-DIG-01", "Termómetro Digital", "Termómetro digital", 4500.0, 18, "un", 8, EstadoStock.DISPONIBLE)

        historialVentas.addAll(
            listOf(
                Venta(
                    folio = "VTA-8492",
                    fecha = "21/10/2026 09:15",
                    cliente = "Clínica RedSalud SpA",
                    items = listOf(VentaItem(prod1, 10), VentaItem(prod2, 2)),
                    vendedorUsername = "vendedor",
                    vendedorNombre = "Luis Gómez",
                    tipoComprobante = "Factura",
                    metodoPago = "Webpay / Transbank",
                    emailCliente = "contacto@redsalud.cl",
                    telefonoCliente = "+56 9 8877 6655",
                    direccionCliente = "Av. Providencia 2000",
                    regionCliente = "Región Metropolitana de Santiago",
                    comunaCliente = "Providencia"
                ),
                Venta(
                    folio = "VTA-3104",
                    fecha = "21/10/2026 11:30",
                    cliente = "María Fernández",
                    items = listOf(VentaItem(prod3, 1), VentaItem(prod1, 5)),
                    vendedorUsername = "gerente",
                    vendedorNombre = "Roberto Silva",
                    tipoComprobante = "Boleta",
                    metodoPago = "Transferencia Bancaria",
                    emailCliente = "m.fernandez@gmail.com",
                    telefonoCliente = "+56 9 5544 3322",
                    direccionCliente = "Calle Los Alerces 45",
                    regionCliente = "Valparaíso",
                    comunaCliente = "Viña del Mar"
                )
            )
        )
    }

    /** Retorna el historial completo de ventas registradas. */
    fun obtenerHistorialVentas(): List<Venta> = historialVentas

    /** Registra una nueva venta completada en el historial. */
    fun registrarVenta(venta: Venta) {
        historialVentas.add(0, venta)
    }

    /** Suma total acumulada en dinero ($) de todas las ventas. */
    fun calcularMontoTotalAcumulado(): Double {
        return historialVentas.sumOf { it.montoTotal }
    }

    /** Total de productos/unidades comercializadas. */
    fun calcularTotalUnidadesVendidas(): Int {
        return historialVentas.sumOf { it.totalItems }
    }

    /** Producto con mayor número de unidades vendidas. */
    fun obtenerProductoMasVendido(): String {
        val mapaCantidades = mutableMapOf<String, Int>()
        for (v in historialVentas) {
            for (item in v.items) {
                val ac = mapaCantidades.getOrDefault(item.producto.nombre, 0)
                mapaCantidades[item.producto.nombre] = ac + item.cantidadVendida
            }
        }
        return mapaCantidades.maxByOrNull { it.value }?.key ?: "Jeringa 10ml"
    }
}
