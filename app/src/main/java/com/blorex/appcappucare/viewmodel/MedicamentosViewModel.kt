
package com.blorex.appcappucare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blorex.appcappucare.model.Medicamento
import com.blorex.appcappucare.repository.MedicamentosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// ViewModel encargado de gestionar las consultas de medicamentos
class MedicamentosViewModel(
    private val repositorio: MedicamentosRepository = MedicamentosRepository()
) : ViewModel() {

    // Medicamento encontrado en Node.js
    var medicamentoEncontrado by mutableStateOf<Medicamento?>(null)
        private set

    // Estado de carga
    var cargando by mutableStateOf(false)
        private set

    // Mensaje para informar el resultado
    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Buscar medicamento mediante el código QR
    fun buscarMedicamento(codigo: String) {

        val codigoLimpio = codigo.trim()

        if (codigoLimpio.isEmpty()) {
            mensajeError = "El código no puede estar vacío"
            medicamentoEncontrado = null
            return
        }

        viewModelScope.launch {

            cargando = true
            mensajeError = null
            medicamentoEncontrado = null

            try {

                medicamentoEncontrado =
                    repositorio.buscarPorCodigo(codigoLimpio)

            } catch (error: HttpException) {

                mensajeError = if (error.code() == 404) {
                    "Medicamento no encontrado en la base de datos"
                } else {
                    "Error del servidor: ${error.code()}"
                }

            } catch (error: IOException) {

                mensajeError =
                    "No se pudo conectar con el servidor Node.js"

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                mensajeError =
                    "Ocurrió un error al consultar el medicamento"

            } finally {

                cargando = false
            }
        }
    }

    // Limpiar los resultados anteriores
    fun limpiarBusqueda() {
        medicamentoEncontrado = null
        mensajeError = null
    }
}
