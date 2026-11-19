package com.blorex.appcappucare.room.dao

import androidx.room.*
import com.blorex.appcappucare.room.entities.FavoritoEntity

/**
 * Data Access Object (DAO) de Room para realizar operaciones sobre la tabla `favoritos`.
 */
@Dao
interface FavoritoDao {

    @Query("SELECT codigo FROM favoritos")
    fun obtenerFavoritosCodigos(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun agregarFavorito(favorito: FavoritoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun agregarTodos(favoritos: List<FavoritoEntity>)

    @Query("DELETE FROM favoritos WHERE codigo = :codigo")
    fun eliminarFavorito(codigo: String)
}
