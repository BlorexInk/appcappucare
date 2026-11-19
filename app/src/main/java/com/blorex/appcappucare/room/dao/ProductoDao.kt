package com.blorex.appcappucare.room.dao

import androidx.room.*
import com.blorex.appcappucare.room.entities.ProductoEntity

/**
 * Data Access Object (DAO) de Room para realizar operaciones sobre la tabla `productos`.
 */
@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos")
    fun obtenerTodos(): List<ProductoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarOActualizar(producto: ProductoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarTodos(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos WHERE codigo = :codigo")
    fun eliminarPorCodigo(codigo: String)
}
