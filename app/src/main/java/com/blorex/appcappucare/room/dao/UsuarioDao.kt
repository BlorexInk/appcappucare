package com.blorex.appcappucare.room.dao

import androidx.room.*
import com.blorex.appcappucare.room.entities.UsuarioEntity

/**
 * Data Access Object (DAO) de Room para realizar operaciones sobre la tabla `usuarios`.
 */
@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios")
    fun obtenerTodosUsuarios(): List<UsuarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarOActualizarUsuario(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarTodos(usuarios: List<UsuarioEntity>)

    @Query("DELETE FROM usuarios WHERE username = :username")
    fun eliminarUsuario(username: String)
}
