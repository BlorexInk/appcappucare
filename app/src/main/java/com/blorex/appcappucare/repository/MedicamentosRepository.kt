
package com.blorex.appcappucare.repository

import com.blorex.appcappucare.model.Medicamento
import com.blorex.appcappucare.network.ClienteMedicamentos
import com.blorex.appcappucare.network.ApiMedicamentos

// Repositorio encargado de consultar medicamentos en Node.js
class MedicamentosRepository(
    private val servicio: ApiMedicamentos = ClienteMedicamentos.servicio
) {

    // Buscar medicamento mediante su código QR
    suspend fun buscarPorCodigo(codigo: String): Medicamento {

        require(codigo.isNotBlank()) {
            "El código del medicamento no puede estar vacío"
        }

        val respuesta = servicio.buscarMedicamento(codigo.trim())

        return respuesta.medicamento
    }

    // Obtener todos los medicamentos registrados
    suspend fun obtenerTodos(): List<Medicamento> {

        val respuesta = servicio.obtenerMedicamentos()

        return respuesta.medicamentos
    }
}
