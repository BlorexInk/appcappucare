package com.blorex.appcappucare.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Modelo de datos que representa una solicitud de modificación de datos enviada por un trabajador al departamento de TI (`blorex`).
 *
 * @property id Identificador único de la solicitud (UUID).
 * @property usernameEmpleado Nombre de usuario que realiza el requerimiento.
 * @property nombreEmpleado Nombre completo del trabajador.
 * @property rolEmpleado Rol o cargo del trabajador.
 * @property asunto Título o resumen de la solicitud.
 * @property detalle Explicación detallada del cambio requerido (ej: cambio de teléfono, dirección o foto).
 * @property fechaSolicitud Registro de fecha y hora de emisión.
 * @property estado Estado del requerimiento ("PENDIENTE", "ATENDIDO").
 */
data class    SolicitudModificacion(
    val id: String = UUID.randomUUID().toString(),
    val usernameEmpleado: String,
    val nombreEmpleado: String,
    val rolEmpleado: String,
    val asunto: String,
    val detalle: String,
    val fechaSolicitud: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
    val estado: String = "PENDIENTE"
)
