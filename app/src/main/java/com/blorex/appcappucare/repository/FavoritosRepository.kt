package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.room.AppRoomDatabase
import com.blorex.appcappucare.room.entities.FavoritoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repositorio encargado de la gestión persistente de productos marcados como Favoritos en Jetpack Room.
 */
object FavoritosRepository {

    private var roomDatabase: AppRoomDatabase? = null
    private val codigosFavoritos = mutableStateListOf<String>()

    /** Inicializa la conexión con Room y carga la lista de favoritos. */
    fun inicializar(context: Context) {
        if (roomDatabase == null) {
            roomDatabase = AppRoomDatabase.getDatabase(context.applicationContext)
            cargarDesdeDb()
        }
    }

    /** Carga los códigos de productos favoritos desde Room. */
    fun cargarDesdeDb() {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                val codigos = db.favoritoDao().obtenerFavoritosCodigos()
                CoroutineScope(Dispatchers.Main).launch {
                    codigosFavoritos.clear()
                    codigosFavoritos.addAll(codigos)
                }
            }
        }
    }

    /** Indica si un código de producto está marcado en favoritos. */
    fun esFavorito(codigo: String): Boolean {
        return codigosFavoritos.contains(codigo.trim().uppercase())
    }

    /** Alterna el estado de favorito de un producto. */
    fun toggleFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        if (esFavorito(cod)) {
            eliminarFavorito(cod)
        } else {
            agregarFavorito(cod)
        }
    }

    /** Agrega un producto a favoritos y lo persiste en Room. */
    fun agregarFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        if (!esFavorito(cod)) {
            roomDatabase?.let { db ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.favoritoDao().agregarFavorito(FavoritoEntity(cod))
                }
            }
            codigosFavoritos.add(cod)
        }
    }

    /** Elimina un producto de favoritos en Room. */
    fun eliminarFavorito(codigo: String) {
        val cod = codigo.trim().uppercase()
        if (esFavorito(cod)) {
            roomDatabase?.let { db ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.favoritoDao().eliminarFavorito(cod)
                }
            }
            codigosFavoritos.remove(cod)
        }
    }

    /** Retorna los objetos [Producto] que coinciden con los códigos favoritos. */
    fun obtenerProductosFavoritos(): List<Producto> {
        val todos = ProductosRepository.getProductosFicticios()
        return todos.filter { esFavorito(it.codigo) }
    }
}
