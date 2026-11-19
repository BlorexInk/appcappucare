package com.blorex.appcappucare.room.dao

import androidx.room.*
import com.blorex.appcappucare.room.entities.SolicitudEntity

/**
 * Data Access Object (DAO) de Room para realizar operaciones sobre la tabla `solicitudes`.
 */
@Dao
interface SolicitudDao {

    @Query("SELECT * FROM solicitudes")
    fun obtenerTodasSolicitudes(): List<SolicitudEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarOActualizarSolicitud(solicitud: SolicitudEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarTodas(solicitudes: List<SolicitudEntity>)
}
