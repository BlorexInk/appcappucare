package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.db.ProductosDatabaseHelper
import com.blorex.appcappucare.model.Producto

/**
 * Repositorio reactivo y persistente para la gestión de productos marcados como favoritos / guardados con el corazón.
 *
 * Sincroniza las adiciones y eliminaciones directamente con la base de datos SQLite/Room
 * para conservar las elecciones del usuario de forma permanente tras reiniciar la app.
 */
object FavoritosRepository {

    private var dbHelper: ProductosDatabaseHelper? = null

    /** Lista reactiva que almacena los códigos de los productos guardados como favoritos. */
    private val favoritosCodigos = mutableStateListOf<String>()

    /**
     * Inicializa la base de datos de favoritos y carga los códigos persistidos.
     *
     * @param context Contexto de la aplicación.
     */
    fun inicializar(context: Context) {
        if (dbHelper == null) {
            dbHelper = ProductosDatabaseHelper(context.applicationContext)
            cargarFavoritosDesdeDb()
        }
    }

    /** Carga la lista de códigos de favoritos guardados físicamente en la base de datos local. */
    fun cargarFavoritosDesdeDb() {
        dbHelper?.let { helper ->
            val favsDb = helper.obtenerFavoritosCodigos()
            favoritosCodigos.clear()
            favoritosCodigos.addAll(favsDb)
        }
    }

    /**
     * Verifica si un producto específico se encuentra en la lista de favoritos.
     *
     * @param codigo Código único del producto.
     * @return `true` si el producto está guardado; `false` de lo contrario.
     */
    fun esFavorito(codigo: String): Boolean {
        return favoritosCodigos.contains(codigo.trim().uppercase())
    }

    /**
     * Alterna el estado de favorito de un producto y lo persiste en la base de datos local SQLite.
     *
     * @param codigo Código del producto a marcar o desmarcar.
     */
    fun toggleFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        if (favoritosCodigos.contains(cod)) {
            eliminarFavorito(cod)
        } else {
            favoritosCodigos.add(cod)
            dbHelper?.agregarFavoritoCodigo(cod)
        }
    }

    /**
     * Elimina un producto de la lista de favoritos y lo remueve físicamente de la base de datos local.
     *
     * @param codigo Código del producto a eliminar.
     */
    fun eliminarFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        favoritosCodigos.remove(cod)
        dbHelper?.eliminarFavoritoCodigo(cod)
    }

    /**
     * Obtiene la lista completa de objetos [Producto] que han sido marcados como favoritos en tiempo real.
     *
     * @return Lista de productos favoritos existentes en el inventario.
     */
    fun obtenerProductosFavoritos(): List<Producto> {
        val todos = ProductosRepository.getProductosFicticios()
        return todos.filter { esFavorito(it.codigo) }
    }
}
