package com.blorex.appcappucare.repository

import com.blorex.appcappucare.model.Movimiento

/**
 * Repositorio de historial de movimientos de inventario (Entradas y Salidas).
 */
object MovimientosRepository {
    private val lista = mutableListOf<Movimiento>()

    /** Retorna el historial de movimientos ordenado del más reciente al más antiguo. */
    fun getMovimientos() = lista.reversed()

    /**
     * Registra un nuevo movimiento en el historial.
     *
     * @param m Objeto [Movimiento] con el detalle de la transacción.
     */
    fun registrarMovimiento(m: Movimiento) {
        lista.add(m)
    }
}
