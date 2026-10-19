package com.blorex.appcappucare.repository

import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.Producto

/**
 * Repositorio reactivo para la gestión de productos marcados como favoritos / guardados con el corazón.
 */
object FavoritosRepository {

    /** Lista reactiva que almacena los códigos de los productos guardados como favoritos. */
    private val favoritosCodigos = mutableStateListOf("MK95-01", "TERM-DIG-01")

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
     * Alterna el estado de favorito de un producto (Agrega si no está, Elimina si ya existe).
     *
     * @param codigo Código del producto a marcar o desmarcar.
     */
    fun toggleFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        if (favoritosCodigos.contains(cod)) {
            favoritosCodigos.remove(cod)
        } else {
            favoritosCodigos.add(cod)
        }
    }

    /**
     * Elimina un producto de la lista de favoritos.
     *
     * @param codigo Código del producto a eliminar.
     */
    fun eliminarFavorito(codigo: String) {
        favoritosCodigos.remove(codigo.trim().uppercase())
    }

    /**
     * Obtiene la lista completa de objetos [Producto] que han sido marcados como favoritos.
     *
     * @return Lista de productos favoritos existentes en el inventario.
     */
    fun obtenerProductosFavoritos(): List<Producto> {
        val todos = ProductosRepository.getProductosFicticios()
        return todos.filter { esFavorito(it.codigo) }
    }
}
