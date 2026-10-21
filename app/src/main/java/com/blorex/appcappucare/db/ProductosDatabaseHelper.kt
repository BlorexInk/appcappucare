package com.blorex.appcappucare.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto

/**
 * Gestor de Base de Da tos Local SQLite/Room para la aplicación AppCappuCare.
 *
 * Almacena los productos y favoritos de forma persistente en el dispositivo móvil, asegurando
 * que los cambios no se pierdan al reiniciar la app.
 */
class ProductosDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "appcappucare.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_PRODUCTOS = "productos"
        private const val COLUMN_CODIGO = "codigo"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_DESCRIPCION = "descripcion"
        private const val COLUMN_VALOR = "valor"
        private const val COLUMN_CANTIDAD = "cantidadDisponible"
        private const val COLUMN_UNIDAD = "unidadMedida"
        private const val COLUMN_MINIMO = "nivelMinimo"
        private const val COLUMN_ESTADO = "estadoStock"
        private const val COLUMN_IMAGENES = "imagenes"

        private const val TABLE_FAVORITOS = "favoritos"
        private const val COLUMN_FAV_CODIGO = "codigo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProductosTable = """
            CREATE TABLE $TABLE_PRODUCTOS (
                $COLUMN_CODIGO TEXT PRIMARY KEY,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_DESCRIPCION TEXT,
                $COLUMN_VALOR REAL NOT NULL,
                $COLUMN_CANTIDAD INTEGER NOT NULL,
                $COLUMN_UNIDAD TEXT NOT NULL,
                $COLUMN_MINIMO INTEGER NOT NULL,
                $COLUMN_ESTADO TEXT NOT NULL,
                $COLUMN_IMAGENES TEXT
            )
        """.trimIndent()

        val createFavoritosTable = """
            CREATE TABLE $TABLE_FAVORITOS (
                $COLUMN_FAV_CODIGO TEXT PRIMARY KEY
            )
        """.trimIndent()

        db.execSQL(createProductosTable)
        db.execSQL(createFavoritosTable)

        precargarProductosIniciales(db)
        precargarFavoritosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        onCreate(db)
    }

    /**
     * Precarga los 20 insumos médicos iniciales en la base de datos local la primera vez que se inicia la app.
     */
    private fun precargarProductosIniciales(db: SQLiteDatabase) {
        val productosIniciales = listOf(
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

        for (p in productosIniciales) {
            val values = ContentValues().apply {
                put(COLUMN_CODIGO, p.codigo)
                put(COLUMN_NOMBRE, p.nombre)
                put(COLUMN_DESCRIPCION, p.descripcion)
                put(COLUMN_VALOR, p.valor)
                put(COLUMN_CANTIDAD, p.cantidadDisponible)
                put(COLUMN_UNIDAD, p.unidadMedida)
                put(COLUMN_MINIMO, p.nivelMinimo)
                put(COLUMN_ESTADO, p.estadoStock.name)
                put(COLUMN_IMAGENES, p.imagenes.joinToString(";"))
            }
            db.insert(TABLE_PRODUCTOS, null, values)
        }
    }

    /** Precarga los favoritos por defecto la primera vez que se inicia la app. */
    private fun precargarFavoritosIniciales(db: SQLiteDatabase) {
        val favs = listOf("MK95-01", "TERM-DIG-01")
        for (cod in favs) {
            val values = ContentValues().apply {
                put(COLUMN_FAV_CODIGO, cod)
            }
            db.insertWithOnConflict(TABLE_FAVORITOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    /** Obtiene todos los productos almacenados en la base de datos local. */
    fun obtenerTodos(): List<Producto> {
        val lista = mutableListOf<Producto>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PRODUCTOS", null)

        if (cursor.moveToFirst()) {
            do {
                val codigo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CODIGO))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION)) ?: ""
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_VALOR))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD))
                val unidad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDAD))
                val minimo = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINIMO))
                val estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ESTADO))
                val imagenesStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGENES)) ?: ""

                val estado = try { EstadoStock.valueOf(estadoStr) } catch (e: Exception) { EstadoStock.DISPONIBLE }
                val listImagenes = if (imagenesStr.isBlank()) emptyList() else imagenesStr.split(";")

                lista.add(
                    Producto(
                        codigo = codigo,
                        nombre = nombre,
                        descripcion = descripcion,
                        valor = valor,
                        cantidadDisponible = cantidad,
                        unidadMedida = unidad,
                        nivelMinimo = minimo,
                        estadoStock = estado,
                        imagenes = listImagenes
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /** Inserta o actualiza un producto en la base de datos local. */
    fun guardarOActualizar(producto: Producto): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODIGO, producto.codigo)
            put(COLUMN_NOMBRE, producto.nombre)
            put(COLUMN_DESCRIPCION, producto.descripcion)
            put(COLUMN_VALOR, producto.valor)
            put(COLUMN_CANTIDAD, producto.cantidadDisponible)
            put(COLUMN_UNIDAD, producto.unidadMedida)
            put(COLUMN_MINIMO, producto.nivelMinimo)
            put(COLUMN_ESTADO, producto.estadoStock.name)
            put(COLUMN_IMAGENES, producto.imagenes.joinToString(";"))
        }

        val result = db.insertWithOnConflict(TABLE_PRODUCTOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    /** Elimina un producto de la base de datos local mediante su código. */
    fun eliminarPorCodigo(codigo: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_PRODUCTOS, "$COLUMN_CODIGO = ?", arrayOf(codigo))
        return rows > 0
    }

    /** Obtiene los códigos de productos marcados como favoritos desde la base de datos local. */
    fun obtenerFavoritosCodigos(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_FAV_CODIGO FROM $TABLE_FAVORITOS", null)
        if (cursor.moveToFirst()) {
            do {
                val cod = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_CODIGO))
                list.add(cod)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    /** Agrega un producto a la lista persistente de favoritos. */
    fun agregarFavoritoCodigo(codigo: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAV_CODIGO, codigo.trim().uppercase())
        }
        val res = db.insertWithOnConflict(TABLE_FAVORITOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return res != -1L
    }

    /** Elimina un producto de la lista persistente de favoritos. */
    fun eliminarFavoritoCodigo(codigo: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_FAVORITOS, "$COLUMN_FAV_CODIGO = ?", arrayOf(codigo.trim().uppercase()))
        return rows > 0
    }
}
