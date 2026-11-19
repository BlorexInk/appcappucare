package com.blorex.appcappucare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blorex.appcappucare.model.Movimiento
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.model.Venta
import com.blorex.appcappucare.model.VentaItem
import com.blorex.appcappucare.repository.MovimientosRepository
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.repository.VentasRepository
import com.blorex.appcappucare.ui.carrito.DatosCheckout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado global del Carrito de Compras, procesamiento de pago con formulario de Checkout (Chile)
 * y validaciones de límites de stock en tiempo real.
 */
class VentaViewModel : ViewModel() {

    /** Lista reactiva de ítems actualmente agregados al carrito de compras. */
    var carrito = mutableStateOf<List<VentaItem>>(emptyList())

    /** Nombre del cliente o empresa asociado a la venta. */
    var cliente = mutableStateOf("Venta Mostrador")

    /** Datos completos capturados durante el checkout de compra (Boleta/Factura, Región/Comuna, Webpay). */
    var datosCheckout = mutableStateOf<DatosCheckout?>(null)

    /** Mensaje de confirmación tras procesar el pago exitosamente. */
    var mensajeConfirmacionPago = mutableStateOf<String?>(null)

    /** Mensaje de advertencia cuando se intenta agregar o incrementar más stock del disponible en sistema. */
    var mensajeAdvertenciaStock = mutableStateOf<String?>(null)

    /** Indica si el pago está en proceso de carga (punto de carga interactivo). */
    var procesandoPago by mutableStateOf(false)
        private set

    fun obtenerStockActual(codigo: String): Int {
        val prod = ProductosRepository.getProductosFicticios().find { it.codigo.equals(codigo, ignoreCase = true) }
        return prod?.cantidadDisponible ?: 0
    }

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

    fun eliminarDelCarrito(producto: Producto) {
        val actual = carrito.value.toMutableList()
        actual.removeAll { it.producto.codigo.equals(producto.codigo, ignoreCase = true) }
        carrito.value = actual
        limpiarAdvertenciaStock()
    }

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

    fun pagarProductosConCarga(
        scope: CoroutineScope,
        datos: DatosCheckout,
        vendedor: Usuario? = null
    ) {
        if (carrito.value.isEmpty() || procesandoPago) return

        datosCheckout.value = datos

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

            delay(4000)

            pagarProductosConCheckout(datos, vendedor)
            procesandoPago = false
        }
    }

    fun pagarProductosConCheckout(datos: DatosCheckout, vendedor: Usuario? = null): Venta? {
        if (carrito.value.isEmpty()) return null

        val nombreCliente = datos.nombreComprador.ifBlank { cliente.value }
        val vUsername = vendedor?.username ?: "vendedor"
        val vNombre = vendedor?.nombreCompleto ?: "Luis Gómez"

        val venta = Venta(
            cliente = nombreCliente,
            items = carrito.value,
            vendedorUsername = vUsername,
            vendedorNombre = vNombre,
            tipoComprobante = datos.tipoComprobante,
            metodoPago = datos.metodoPago,
            emailCliente = datos.email,
            telefonoCliente = datos.telefono,
            direccionCliente = datos.direccionEnvio,
            regionCliente = datos.regionChile,
            comunaCliente = datos.comunaChile
        )

        // Registrar en historial de movimientos
        venta.items.forEach { item ->
            MovimientosRepository.registrarMovimiento(
                Movimiento(
                    codigoProducto = item.producto.codigo,
                    nombreProducto = item.producto.nombre,
                    tipo = "SALIDA",
                    cantidad = -item.cantidadVendida,
                    motivo = "Venta Pagada (${datos.tipoComprobante} - ${datos.metodoPago})",
                    cliente = venta.cliente
                )
            )
            val prodActual = ProductosRepository.getProductosFicticios().find { it.codigo == item.producto.codigo }
            if (prodActual != null) {
                val nuevaCant = (prodActual.cantidadDisponible - item.cantidadVendida).coerceAtLeast(0)
                ProductosRepository.actualizarProducto(prodActual.copy(cantidadDisponible = nuevaCant))
            }
        }

        // Registrar en historial de ventas
        VentasRepository.registrarVenta(venta)

        val detalleEnvio = if (datos.esEnvioDomicilio) "Despacho a ${datos.comunaChile}, ${datos.regionChile}" else "Retiro en Mostrador/Bodega"
        mensajeConfirmacionPago.value = "¡Pago procesado con éxito ($${datos.metodoPago})! Comprobante: ${datos.tipoComprobante} Folio #${venta.folio} emitido a ${datos.nombreComprador}. ($detalleEnvio)."
        carrito.value = emptyList()
        return venta
    }

    fun finalizarVenta(): Venta {
        val datosDefault = datosCheckout.value ?: DatosCheckout(
            nombreComprador = cliente.value,
            telefono = "",
            email = "",
            tipoComprobante = "Boleta"
        )
        return pagarProductosConCheckout(datosDefault) ?: Venta(cliente = cliente.value, items = emptyList())
    }

    fun limpiarMensaje() {
        mensajeConfirmacionPago.value = null
    }

    fun limpiarAdvertenciaStock() {
        mensajeAdvertenciaStock.value = null
    }
}
