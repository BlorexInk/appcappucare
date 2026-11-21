
package com.blorex.appcappucare.model

// Información de un medicamento registrado en Node.js
data class Medicamento(
    val codigo: String,
    val nombre: String,
    val principioActivo: String,
    val concentracion: String,
    val laboratorio: String,
    val stock: Int
)

// Respuesta del servidor al buscar un medicamento
data class RespuestaMedicamento(
    val medicamento: Medicamento
)

// Respuesta del servidor al listar medicamentos
data class RespuestaListaMedicamentos(
    val medicamentos: List<Medicamento>
)
