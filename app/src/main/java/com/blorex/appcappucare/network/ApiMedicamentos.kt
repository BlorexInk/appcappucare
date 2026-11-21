
package com.blorex.appcappucare.network

import com.blorex.appcappucare.model.RespuestaMedicamento
import com.blorex.appcappucare.model.RespuestaListaMedicamentos
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Endpoints para consultar medicamentos en Node.js
interface ApiMedicamentos {

    // Buscar medicamento mediante el código QR
    @GET("api/medicamentos/codigo/{codigo}")
    suspend fun buscarMedicamento(
        @Path("codigo") codigo: String
    ): RespuestaMedicamento

    // Obtener todos los medicamentos registrados
    @GET("api/medicamentos")
    suspend fun obtenerMedicamentos(): RespuestaListaMedicamentos
}

// Configuración de conexión con Node.js
object ClienteMedicamentos {

    // Dirección del backend desde el emulador Android
    private const val URL_BASE = "http://10.0.2.2:3000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicio para consultar la API
    val servicio: ApiMedicamentos by lazy {
        retrofit.create(ApiMedicamentos::class.java)
    }
}
