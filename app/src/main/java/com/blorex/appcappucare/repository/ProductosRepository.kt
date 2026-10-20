package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.db.ProductosDatabaseHelper
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto

/**
 * Repositorio principal de productos conectado con la base de datos local SQLite/Room.
 *
 * Mantiene un estado reactivo en tiempo real ([mutableStateListOf]) sincronizado con la base de datos local
 * y ofrece generación automática de códigos por nombre de producto y operaciones CRUD.
 */
object ProductosRepository {

    private var dbHelper: ProductosDatabaseHelper? = null
    private val productos = mutableStateListOf<Producto>()

    /**
     * Inicializa la conexión con la base de datos local y carga los productos persistidos.
     *
     * @param context Contexto de la aplicación.
     */
    fun inicializarBaseDeDatos(context: Context) {
        if (dbHelper == null) {
            dbHelper = ProductosDatabaseHelper(context.applicationContext)
            cargarDesdeBaseDeDatos()
        }
    }

    /** Carga o recarga la lista de productos desde la base de datos local SQLite. */
    private fun cargarDesdeBaseDeDatos() {
        dbHelper?.let { helper ->
            val lista = helper.obtenerTodos()
            productos.clear()
            productos.addAll(lista)
        }
    }

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

    /**
     * Genera automáticamente un código predefinido basado en el nombre del producto.
     * Ejemplo: "Jeringa 10ml" -> "JER-10ML-842"
     *
     * @param nombre Nombre ingresado del producto.
     * @return Código estandarizado único.
     */
    fun generarCodigoPredefinido(nombre: String): String {
        val limpio = nombre.trim().uppercase().replace(Regex("[^A-Z0-9 ]"), "")
        if (limpio.isBlank()) return "PROD-${(100..999).random()}"

        val palabras = limpio.split("\\s+".toRegex()).take(3)
        val prefijo = palabras.joinToString("-") { palabra ->
            if (palabra.length >= 3) palabra.take(3) else palabra
        }

        val sufijoAleatorio = (100..999).random()
        val codigoBase = "$prefijo-$sufijoAleatorio"

        // Garantizar unicidad
        if (productos.any { it.codigo.equals(codigoBase, ignoreCase = true) }) {
            return "$prefijo-${(1000..9999).random()}"
        }
        return codigoBase
    }

    /** Retorna la lista reactiva completa de productos. */
    fun getProductosFicticios(): List<Producto> = productos

    /**
     * Agrega un nuevo producto al inventario y lo persiste en la base de datos local.
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

        val insertadoDb = dbHelper?.guardarOActualizar(nuevoProducto) ?: true
        if (insertadoDb) {
            productos.add(nuevoProducto)
            return true
        }
        return false
    }

    /**
     * Actualiza la información y recalcula el estado de stock de un producto existente en la base de datos.
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
            dbHelper?.guardarOActualizar(productoActualizado)
            productos[index] = productoActualizado
            return true
        }
        return false
    }

    /**
     * Elimina un producto del catálogo y de la base de datos local mediante su código único.
     *
     * @param codigo Código del producto a eliminar.
     * @return `true` si se eliminó correctamente; `false` de lo contrario.
     */
    fun eliminarProducto(codigo: String): Boolean {
        val eliminadoDb = dbHelper?.eliminarPorCodigo(codigo) ?: true
        if (eliminadoDb) {
            return productos.removeIf { it.codigo.equals(codigo, ignoreCase = true) }
        }
        return false
    }
}
