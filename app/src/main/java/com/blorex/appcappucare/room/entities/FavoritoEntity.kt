package com.blorex.appcappucare.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla `favoritos` en la base de datos local SQLite.
 */
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey
    val codigo: String
)
