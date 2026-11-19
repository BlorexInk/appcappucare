package com.blorex.appcappucare.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blorex.appcappucare.model.SolicitudModificacion

/**
 * Entidad de Room que representa la tabla `solicitudes` en la base de datos local SQLite.
 */
@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey
    val id: String,
    val usernameEmpleado: String,
    val nombreEmpleado: String,
    val rolEmpleado: String,
    val asunto: String,
    val detalle: String,
    val fechaSolicitud: String,
    val estado: String
) {
    /** Convierte esta entidad de Room al modelo de dominio [SolicitudModificacion]. */
    fun toDomainModel(): SolicitudModificacion {
        return SolicitudModificacion(
            id = id,
            usernameEmpleado = usernameEmpleado,
            nombreEmpleado = nombreEmpleado,
            rolEmpleado = rolEmpleado,
            asunto = asunto,
            detalle = detalle,
            fechaSolicitud = fechaSolicitud,
            estado = estado
        )
    }

    companion object {
        /** Mapea una [SolicitudModificacion] a [SolicitudEntity]. */
        fun fromDomainModel(s: SolicitudModificacion): SolicitudEntity {
            return SolicitudEntity(
                id = s.id,
                usernameEmpleado = s.usernameEmpleado,
                nombreEmpleado = s.nombreEmpleado,
                rolEmpleado = s.rolEmpleado,
                asunto = s.asunto,
                detalle = s.detalle,
                fechaSolicitud = s.fechaSolicitud,
                estado = s.estado
            )
        }
    }
}
