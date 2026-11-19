package com.blorex.appcappucare.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla `configuracion` en la base de datos local SQLite.
 */
@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey
    val clave: String,
    val valor: String
)
