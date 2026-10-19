package com.blorex.appcappucare.repository

import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto

/**
 * Repositorio principal de productos e inventario.
 *
 * Mantiene un estado reactivo en tiempo real ([mutableStateListOf]) con un catálogo inicial
 * de 20 insumos médicos y ofrece operaciones CRUD (Crear, Leer, Actualizar y Eliminar).
 */
object ProductosRepository {

    /**
     * Evalúa la cantidad disponible frente al nivel mínimo para determinar el [EstadoStock].
     *
     * @param cantidad Cantidad actual disponible.
     * @param nivelMinimo Nivel de seguridad de stock.
     * @return [EstadoStock.AGOTADO] si la cantidad <= 0, [EstadoStock.STOCK_BAJO] si cantidad <= nivelMinimo, o [EstadoStock.DISPONIBLE].
     */
    fun calcularEstadoStock(cantidad: Int, nivelMinimo: Int): EstadoStock {
        return when {
            cantidad <= 0 -> EstadoStock.AGOTADO
            cantidad <= nivelMinimo -> EstadoStock.STOCK_BAJO
            else -> EstadoStock.DISPONIBLE
        }
    }

    /** Lista reactiva de 20 productos iniciales de la aplicación. */
    private val productos = mutableStateListOf(
        Producto("JR-10ML", "Jeringa 10ml", 45, "un", 20, EstadoStock.DISPONIBLE),
        Producto("MK95-01", "Mascarilla KN95", 5, "un", 20, EstadoStock.STOCK_BAJO),
        Producto("GN-NL-M", "Guantes Nitrilo M", 0, "caja", 10, EstadoStock.AGOTADO),
        Producto("GS-EST-10", "Gasas Estériles", 100, "paq", 30, EstadoStock.DISPONIBLE),
        Producto("ALC-70-1L", "Alcohol Etílico 70% 1L", 25, "frasco", 10, EstadoStock.DISPONIBLE),
        Producto("OXI-PUL-01", "Oxímetro de Pulso", 12, "un", 5, EstadoStock.DISPONIBLE),
        Producto("TERM-DIG-01", "Termómetro Digital", 18, "un", 8, EstadoStock.DISPONIBLE),
        Producto("VEN-ELA-10", "Venda Elástica 10cm", 30, "un", 15, EstadoStock.DISPONIBLE),
        Producto("SOL-SAL-500", "Solución Salina 0.9% 500ml", 50, "un", 20, EstadoStock.DISPONIBLE),
        Producto("ALG-HYG-500", "Algodón Hidrófilo 500g", 15, "paq", 10, EstadoStock.DISPONIBLE),
        Producto("CAT-IV-20G", "Catéter Intravenoso 20G", 80, "un", 25, EstadoStock.DISPONIBLE),
        Producto("TENS-DIG-01", "Tensiómetro Digital", 8, "un", 4, EstadoStock.DISPONIBLE),
        Producto("JAB-ANT-1L", "Jabón Antiséptico 1L", 22, "frasco", 10, EstadoStock.DISPONIBLE),
        Producto("CINT-MIC-25", "Cinta Micropore 2.5cm", 40, "un", 15, EstadoStock.DISPONIBLE),
        Producto("ESPAD-10", "Esparadrapo de Tela", 35, "un", 10, EstadoStock.DISPONIBLE),
        Producto("JERING-03ML", "Jeringa 3ml con Aguja", 120, "un", 40, EstadoStock.DISPONIBLE),
        Producto("GUA-LAT-L", "Guantes de Látex L", 3, "caja", 10, EstadoStock.STOCK_BAJO),
        Producto("GOR-DES-100", "Gorros Desechables", 200, "un", 50, EstadoStock.DISPONIBLE),
        Producto("ZAP-DES-100", "Cubrezapatos Desechables", 150, "un", 40, EstadoStock.DISPONIBLE),
        Producto("SUT-NIR-30", "Hilo de Sutura Nylon 3-0", 0, "un", 12, EstadoStock.AGOTADO)
    )

    /** Retorna la lista reactiva completa de productos. */
    fun getProductosFicticios(): List<Producto> = productos

    /**
     * Agrega un nuevo producto al inventario validando que el código no exista previamente.
     *
     * @param producto Insumo a registrar.
     * @return `true` si fue agregado exitosamente; `false` si el código ya existía.
     */
    fun agregarProducto(producto: Producto): Boolean {
        if (productos.any { it.codigo.equals(producto.codigo, ignoreCase = true) }) {
            return false
        }
        val nuevoProducto = producto.copy(
            estadoStock = calcularEstadoStock(producto.cantidadDisponible, producto.nivelMinimo)
        )
        productos.add(nuevoProducto)
        return true
    }

    /**
     * Actualiza la información y recalcula el estado de stock de un producto existente.
     *
     * @param producto Insumo con datos actualizados.
     * @return `true` si se encontró y actualizó el producto; `false` en caso contrario.
     */
    fun actualizarProducto(producto: Producto): Boolean {
        val index = productos.indexOfFirst { it.codigo.equals(producto.codigo, ignoreCase = true) }
        if (index != -1) {
            val productoActualizado = producto.copy(
                estadoStock = calcularEstadoStock(producto.cantidadDisponible, producto.nivelMinimo)
            )
            productos[index] = productoActualizado
            return true
        }
        return false
    }

    /**
     * Elimina un producto del catálogo mediante su código único.
     *
     * @param codigo Código del producto a eliminar.
     * @return `true` si se eliminó correctamente; `false` de lo contrario.
     */
    fun eliminarProducto(codigo: String): Boolean {
        return productos.removeIf { it.codigo.equals(codigo, ignoreCase = true) }
    }
}
