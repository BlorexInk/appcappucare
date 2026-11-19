package com.blorex.appcappucare.room.dao

import androidx.room.*
import com.blorex.appcappucare.room.entities.ConfiguracionEntity

/**
 * Data Access Object (DAO) de Room para realizar operaciones sobre la tabla `configuracion`.
 */
@Dao
interface ConfiguracionDao {

    @Query("SELECT * FROM configuracion")
    fun obtenerConfiguracion(): List<ConfiguracionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarConfiguracion(config: ConfiguracionEntity)
}
