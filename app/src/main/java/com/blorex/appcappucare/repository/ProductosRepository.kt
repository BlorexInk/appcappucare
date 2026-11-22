package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.room.AppRoomDatabase
import com.blorex.appcappucare.room.entities.ProductoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.blorex.appcappucare.repository.sync.HttpSyncClient
import com.blorex.appcappucare.repository.sync.ModoConexion
import com.blorex.appcappucare.repository.sync.SyncBackendRepository

/**
 * Repositorio principal de productos conectado con la base de datos local Jetpack Room.
 *
 * Mantiene un estado reactivo en tiempo real ([mutableStateListOf]) sincronizado con Room
 * y ofrece generación automática de códigos por nombre de producto y operaciones CRUD.
 */
object ProductosRepository {

    private var roomDatabase: AppRoomDatabase? = null
    private val productos = mutableStateListOf<Producto>()

    val productosInicialesDefecto = listOf(
        Producto("JR-10ML", "Jeringa 10ml", "Jeringa desechable estéril de 10ml para inyecciones y extracción de fluidos.", 1200.0, 45, "un", 20, EstadoStock.DISPONIBLE, listOf("jeringa_1.jpg", "jeringa_2.jpg")),
        Producto("MK95-01", "Mascarilla KN95", "Mascarilla de protección respiratoria KN95 de 5 capas de alta filtración.", 800.0, 5, "un", 20, EstadoStock.STOCK_BAJO, listOf("mascarilla_1.jpg")),
        Producto("GN-NL-M", "Guantes Nitrilo M", "Caja con 100 guantes de nitrilo sin polvo tamaño Mediano para examen médico.", 8500.0, 0, "caja", 10, EstadoStock.AGOTADO, listOf("guantes_1.jpg", "guantes_2.jpg")),
        Producto("GS-EST-10", "Gasas Estériles", "Paquete con 10 sobres de gasa estéril de tejido 100% algodón 10x10cm.", 1500.0, 100, "paq", 30, EstadoStock.DISPONIBLE, listOf("gasa_1.jpg")),
        Producto("ALC-70-1L", "Alcohol Etílico 70% 1L", "Frasco de alcohol antiséptico de 70 grados de 1 Litro para desinfección.", 3200.0, 25, "frasco", 10, EstadoStock.DISPONIBLE, listOf("alcohol_1.jpg")),
        Producto("OXI-PUL-01", "Oxímetro de Pulso", "Oxímetro de pulso para dedo digital de alta precisión con pantalla OLED.", 24900.0, 12, "un", 5, EstadoStock.DISPONIBLE, listOf("oximetro_1.jpg", "oximetro_2.jpg")),
        Producto("TERM-DIG-01", "Termómetro Digital", "Termómetro digital corporal de lectura rápida con alarma de fiebre.", 4500.0, 18, "un", 8, EstadoStock.DISPONIBLE, listOf("termometro_1.jpg")),
        Producto("VEN-ELA-10", "Venda Elástica 10cm", "Venda elástica de compresión médica de 10cm x 5m con sujetadores.", 2100.0, 30, "un", 15, EstadoStock.DISPONIBLE, listOf("venda_1.jpg")),
        Producto("SOL-SAL-500", "Solución Salina 0.9% 500ml", "Solución fisiológica de cloruro de sodio 0.9% para irrigación o suero.", 2800.0, 50, "un", 20, EstadoStock.DISPONIBLE, listOf("suero_1.jpg")),
        Producto("ALG-HYG-500", "Algodón Hidrófilo 500g", "Paquete de algodón hidrófilo de 500 gramos 100% puro para curaciones.", 3900.0, 15, "paq", 10, EstadoStock.DISPONIBLE, listOf("algodon_1.jpg")),
        Producto("CAT-IV-20G", "Catéter Intravenoso 20G", "Catéter periférico intravenoso 20G con teflón estéril de seguridad.", 1900.0, 80, "un", 25, EstadoStock.DISPONIBLE, listOf("cateter_1.jpg")),
        Producto("TENS-DIG-01", "Tensiómetro Digital", "Monitor de presión arterial automático de brazo con memoria de lecturas.", 38900.0, 8, "un", 4, EstadoStock.DISPONIBLE, listOf("tensiometro_1.jpg", "tensiometro_2.jpg")),
        Producto("JAB-ANT-1L", "Jabón Antiséptico 1L", "Jabón líquido quirúrgico y antiséptico de clorhexidina 1L.", 6800.0, 22, "frasco", 10, EstadoStock.DISPONIBLE, listOf("jabon_1.jpg")),
        Producto("CINT-MIC-25", "Cinta Micropore 2.5cm", "Cinta médica quirúrgica de papel hipoalergénico 2.5cm x 10m.", 1600.0, 40, "un", 15, EstadoStock.DISPONIBLE, listOf("cinta_1.jpg")),
        Producto("ESPAD-10", "Esparadrapo de Tela", "Rollo de esparadrapo de tela resistente de fijación 5cm x 5m.", 2300.0, 35, "un", 10, EstadoStock.DISPONIBLE, listOf("esparadrapo_1.jpg")),
        Producto("JERING-03ML", "Jeringa 3ml con Aguja", "Jeringa desechable de 3ml con aguja hipodérmica 21G incorporada.", 950.0, 120, "un", 40, EstadoStock.DISPONIBLE, listOf("jeringa3ml_1.jpg")),
        Producto("GUA-LAT-L", "Guantes de Látex L", "Caja con 100 guantes de examen de látex natural estandarizado tamaño L.", 7900.0, 3, "caja", 10, EstadoStock.STOCK_BAJO, listOf("guantes_latex_1.jpg")),
        Producto("GOR-DES-100", "Gorros Desechables", "Bolsa con 100 gorros oruga desechables de polipropileno para clínica.", 4200.0, 200, "un", 50, EstadoStock.DISPONIBLE, listOf("gorros_1.jpg")),
        Producto("ZAP-DES-100", "Cubrezapatos Desechables", "Bolsa con 100 cubrezapatos antideslizantes desechables azulin.", 4800.0, 150, "un", 40, EstadoStock.DISPONIBLE, listOf("cubrezapatos_1.jpg")),
        Producto("SUT-NIR-30", "Hilo de Sutura Nylon 3-0", "Sobre con hilo de sutura monofilamento de nylon 3-0 con aguja corta.", 3500.0, 0, "un", 12, EstadoStock.AGOTADO, listOf("sutura_1.jpg"))
    )

    /**
     * Inicializa la conexión con la base de datos de Room y carga los productos persistidos.
     *
     * @param context Contexto de la aplicación.
     */
    fun inicializarBaseDeDatos(context: Context) {
        if (roomDatabase == null) {
            roomDatabase = AppRoomDatabase.getDatabase(context.applicationContext)
            cargarDesdeBaseDeDatos()
        }
    }

    /** Carga o recarga la lista de productos desde la base de datos local Room. */
    fun cargarDesdeBaseDeDatos() {
        val db = roomDatabase
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val entidades = db.productoDao().obtenerTodos()
                val lista = entidades.map { it.toDomainModel() }
                CoroutineScope(Dispatchers.Main).launch {
                    productos.clear()
                    if (lista.isEmpty()) {
                        productos.addAll(productosInicialesDefecto)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.productoDao().guardarTodos(productosInicialesDefecto.map { ProductoEntity.fromDomainModel(it) })
                        }
                    } else {
                        productos.addAll(lista)
                    }
                }
            }
        } else {
            if (productos.isEmpty()) {
                productos.addAll(productosInicialesDefecto)
            }
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

        if (productos.any { it.codigo.equals(codigoBase, ignoreCase = true) }) {
            return "$prefijo-${(1000..9999).random()}"
        }
        return codigoBase
    }

    /** Retorna la lista reactiva completa de productos. */
    fun getProductosFicticios(): List<Producto> {
        if (productos.isEmpty()) {
            productos.addAll(productosInicialesDefecto)
        }
        return productos
    }

    /**
     * Agrega un nuevo producto al inventario y lo persiste en la base de datos de Room.
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

        val db = roomDatabase
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                db.productoDao().guardarOActualizar(ProductoEntity.fromDomainModel(nuevoProducto))
            }
            productos.add(nuevoProducto)
            return true
        } else {
            productos.add(nuevoProducto)
            return true
        }
    }

    /**
     * Actualiza la información y recalcula el estado de stock de un producto existente en Room.
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

            roomDatabase?.let { db ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.productoDao().guardarOActualizar(ProductoEntity.fromDomainModel(productoActualizado))
                }
            }
            productos[index] = productoActualizado
            return true
        }
        return false
    }

    /**
     * Elimina un producto del catálogo y de la base de datos de Room mediante su código único.
     *
     * @param codigo Código del producto a eliminar.
     * @return `true` si se eliminó correctamente; `false` de lo contrario.
     */

    fun eliminarProducto(codigo: String): Boolean {
        val eliminado = productos.removeIf {
            it.codigo.equals(codigo, ignoreCase = true)
        }

        if (!eliminado) return false

        val db = roomDatabase

        CoroutineScope(Dispatchers.IO).launch {
            db?.productoDao()?.eliminarPorCodigo(codigo)
            eliminarProductoRemoto(codigo)
        }

        return true
    }


    private fun eliminarProductoRemoto(codigo: String) {
        if (SyncBackendRepository.modoConexion != ModoConexion.ONLINE_BACKEND) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val eliminado = HttpSyncClient.eliminarProductoServidor(
                baseUrl = SyncBackendRepository.servidorUrlInput
                    .trim()
                    .removeSuffix("/"),
                codigo = codigo
            )

            if (!eliminado) {
                android.util.Log.e(
                    "CappuCareSync",
                    "No se pudo eliminar el producto $codigo del servidor"
                )
            }
        }
    }
}
