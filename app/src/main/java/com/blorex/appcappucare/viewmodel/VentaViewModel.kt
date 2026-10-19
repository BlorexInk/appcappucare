package com.blorex.appcappucare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blorex.appcappucare.model.Movimiento
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.model.Venta
import com.blorex.appcappucare.model.VentaItem
import com.blorex.appcappucare.repository.MovimientosRepository
import com.blorex.appcappucare.repository.ProductosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado global del Carrito de Compras, el procesamiento de ventas
 * y las validaciones de límites de stock en tiempo real.
 */
class VentaViewModel : ViewModel() {

    /** Lista reactiva de ítems actualmente agregados al carrito de compras. */
    var carrito = mutableStateOf<List<VentaItem>>(emptyList())

    /** Nombre del cliente asociado a la venta. */
    var cliente = mutableStateOf("Venta Mostrador")

    /** Mensaje de confirmación tras procesar el pago exitosamente. */
    var mensajeConfirmacionPago = mutableStateOf<String?>(null)

    /** Mensaje de advertencia cuando se intenta agregar o incrementar más stock del disponible en sistema. */
    var mensajeAdvertenciaStock = mutableStateOf<String?>(null)

    /** Indica si el pago está en proceso de carga (punto de carga interactivo). */
    var procesandoPago by mutableStateOf(false)
        private set

    /**
     * Obtiene el stock actual disponible en tiempo real desde el repositorio para un producto.
     *
     * @param codigo Código del producto.
     * @return Cantidad disponible en sistema (0 si no se encuentra).
     */
    fun obtenerStockActual(codigo: String): Int {
        val prod = ProductosRepository.getProductosFicticios().find { it.codigo.equals(codigo, ignoreCase = true) }
        return prod?.cantidadDisponible ?: 0
    }

    /**
     * Agrega un producto al carrito o incrementa su cantidad validando estrictamente el stock disponible.
     *
     * @param producto Producto a agregar.
     * @param cantidad Cantidad inicial (por defecto 1).
     * @return `true` si se agregó correctamente; `false` si superó el stock disponible.
     */
    fun agregarAlCarrito(producto: Producto, cantidad: Int = 1): Boolean {
        val stockDisponible = obtenerStockActual(producto.codigo)

        if (stockDisponible <= 0) {
            mensajeAdvertenciaStock.value = "El producto '${producto.nombre}' está agotado y no está disponible para su venta."
            return false
        }

        val actual = carrito.value.toMutableList()
        val ex = actual.find { it.producto.codigo.equals(producto.codigo, ignoreCase = true) }
        val cantidadActualEnCarrito = ex?.cantidadVendida ?: 0
        val cantidadDeseada = cantidadActualEnCarrito + cantidad

        if (cantidadDeseada > stockDisponible) {
            mensajeAdvertenciaStock.value = "No hay más producto disponible que lo indicado en sistema. Máximo disponible: $stockDisponible ${producto.unidadMedida}."
            if (ex != null) {
                ex.cantidadVendida = stockDisponible
                carrito.value = actual
            }
            return false
        }

        if (ex != null) {
            ex.cantidadVendida = cantidadDeseada
        } else {
            actual.add(VentaItem(producto, cantidadDeseada))
        }

        carrito.value = actual
        mensajeAdvertenciaStock.value = null
        return true
    }

    /**
     * Elimina un producto por completo del carrito de compras.
     *
     * @param producto Producto a remover.
     */
    fun eliminarDelCarrito(producto: Producto) {
        val actual = carrito.value.toMutableList()
        actual.removeAll { it.producto.codigo.equals(producto.codigo, ignoreCase = true) }
        carrito.value = actual
        limpiarAdvertenciaStock()
    }

    /**
     * Incrementa en 1 la cantidad solicitada de un producto en el carrito validando el stock en tiempo real.
     *
     * @param producto Producto a incrementar.
     */
    fun incrementarCantidad(producto: Producto) {
        val stockDisponible = obtenerStockActual(producto.codigo)
        val actual = carrito.value.toMutableList()
        val ex = actual.find { it.producto.codigo.equals(producto.codigo, ignoreCase = true) }

        if (ex != null) {
            if (ex.cantidadVendida >= stockDisponible) {
                mensajeAdvertenciaStock.value = "No hay más producto disponible que lo indicado en sistema ($stockDisponible ${producto.unidadMedida})."
            } else {
                ex.cantidadVendida += 1
                carrito.value = actual
                mensajeAdvertenciaStock.value = null
            }
        }
    }

    /**
     * Disminuye en 1 la cantidad solicitada de un producto (elimina del carrito si llega a 0).
     *
     * @param producto Producto a decrementar.
     */
    fun decrementarCantidad(producto: Producto) {
        val actual = carrito.value.toMutableList()
        val ex = actual.find { it.producto.codigo.equals(producto.codigo, ignoreCase = true) }

        if (ex != null) {
            if (ex.cantidadVendida > 1) {
                ex.cantidadVendida -= 1
                carrito.value = actual
            } else {
                eliminarDelCarrito(producto)
            }
            mensajeAdvertenciaStock.value = null
        }
    }

    /**
     * Inicia el proceso de pago con una simulación de carga interactiva de 4 segundos.
     *
     * @param scope Scope de corrutinas para ejecutar la simulación en segundo plano.
     */
    fun pagarProductosConCarga(scope: CoroutineScope) {
        if (carrito.value.isEmpty() || procesandoPago) return

        // Validar que ningún producto en carrito exceda el stock actual
        val itemInvalido = carrito.value.find { item ->
            val stockActual = obtenerStockActual(item.producto.codigo)
            stockActual <= 0 || item.cantidadVendida > stockActual
        }

        if (itemInvalido != null) {
            val stockActual = obtenerStockActual(itemInvalido.producto.codigo)
            if (stockActual <= 0) {
                mensajeAdvertenciaStock.value = "El producto '${itemInvalido.producto.nombre}' se encuentra agotado. Elimínalo para continuar."
            } else {
                mensajeAdvertenciaStock.value = "El producto '${itemInvalido.producto.nombre}' excede el stock disponible ($stockActual ${itemInvalido.producto.unidadMedida})."
            }
            return
        }

        scope.launch {
            procesandoPago = true
            limpiarMensaje()

            // Simulación de punto de carga de 4 segundos (3 a 5 seg)
            delay(4000)

            pagarProductos()
            procesandoPago = false
        }
    }

    /**
     * Procesa la compra de los productos en el carrito:
     * - Genera el registro de venta con número de folio.
     * - Registra los movimientos de salida en el historial.
     * - Descuenta la cantidad vendida del inventario en [ProductosRepository].
     * - Limpia el carrito de compras.
     *
     * @return Objeto [Venta] con el folio generado; `null` si el carrito estaba vacío.
     */
    fun pagarProductos(): Venta? {
        if (carrito.value.isEmpty()) return null

        val venta = Venta(cliente = cliente.value, items = carrito.value)
        venta.items.forEach { item ->
            MovimientosRepository.registrarMovimiento(
                Movimiento(
                    codigoProducto = item.producto.codigo,
                    nombreProducto = item.producto.nombre,
                    tipo = "SALIDA",
                    cantidad = -item.cantidadVendida,
                    motivo = "Venta Pagada",
                    cliente = venta.cliente
                )
            )
            val prodActual = ProductosRepository.getProductosFicticios().find { it.codigo == item.producto.codigo }
            if (prodActual != null) {
                val nuevaCant = (prodActual.cantidadDisponible - item.cantidadVendida).coerceAtLeast(0)
                ProductosRepository.actualizarProducto(prodActual.copy(cantidadDisponible = nuevaCant))
            }
        }

        mensajeConfirmacionPago.value = "¡Pago procesado con éxito! Folio de Venta: ${venta.folio}"
        carrito.value = emptyList()
        return venta
    }

    /** Alias para mantener compatibilidad con flujos de venta rápida. */
    fun finalizarVenta(): Venta {
        return pagarProductos() ?: Venta(cliente = cliente.value, items = emptyList())
    }

    /** Oculta el mensaje de confirmación de pago. */
    fun limpiarMensaje() {
        mensajeConfirmacionPago.value = null
    }

    /** Limpia el mensaje de advertencia de stock. */
    fun limpiarAdvertenciaStock() {
        mensajeAdvertenciaStock.value = null
    }
}
