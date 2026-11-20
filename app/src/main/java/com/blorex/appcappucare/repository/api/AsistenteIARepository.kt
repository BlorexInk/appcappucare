package com.blorex.appcappucare.repository.api

import com.blorex.appcappucare.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Producto que la IA recomienda (el servidor ya verificó que existe en el catálogo). */
data class ProductoSugerido(
    val codigo: String,
    val nombre: String,
    val valor: Double,
    val estadoStock: String,
    val cantidad: Int
)

/** Mensaje de la conversación con el asistente. */
data class MensajeIA(
    val esUsuario: Boolean,
    val texto: String,
    val sugeridos: List<ProductoSugerido> = emptyList()
)

data class RespuestaIA(
    val texto: String,
    val sugeridos: List<ProductoSugerido>
)

sealed class ResultadoIA {
    data class Exito(val respuesta: RespuestaIA) : ResultadoIA()
    data class Error(val mensaje: String) : ResultadoIA()
}

/**
 * Cliente del asistente de IA. Habla con el backend propio (POST /api/ia/chat),
 * que es quien guarda la clave de la API y llama a Gemini o Claude.
 * La app nunca contiene la clave de la IA.
 */
object AsistenteIARepository {

    suspend fun preguntar(
        baseUrl: String,
        mensaje: String,
        historial: List<MensajeIA>,
        catalogo: List<Producto>
    ): ResultadoIA = withContext(Dispatchers.IO) {
        var conexion: HttpURLConnection? = null
        try {
            val cuerpo = JSONObject().apply {
                put("mensaje", mensaje)
                put("historial", JSONArray().apply {
                    historial.takeLast(6).forEach { m ->
                        put(JSONObject().apply {
                            put("rol", if (m.esUsuario) "usuario" else "asistente")
                            put("texto", m.texto)
                        })
                    }
                })
                put("catalogo", JSONArray().apply {
                    catalogo.forEach { p ->
                        put(JSONObject().apply {
                            put("codigo", p.codigo)
                            put("nombre", p.nombre)
                            put("descripcion", p.descripcion)
                            put("valor", p.valor)
                            put("cantidadDisponible", p.cantidadDisponible)
                            put("estadoStock", p.estadoStock.name)
                        })
                    }
                })
            }

            conexion = (URL("$baseUrl/api/ia/chat").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 90000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            conexion.outputStream.use { it.write(cuerpo.toString().toByteArray(Charsets.UTF_8)) }

            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            val json = try { JSONObject(texto) } catch (e: Exception) { null }

            if (codigo !in 200..299) {
                return@withContext ResultadoIA.Error(
                    json?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "El servidor respondió con error ($codigo)."
                )
            }
            if (json == null) {
                return@withContext ResultadoIA.Error("Respuesta inválida del servidor.")
            }

            val sugeridos = mutableListOf<ProductoSugerido>()
            val arreglo = json.optJSONArray("productos") ?: JSONArray()
            for (i in 0 until arreglo.length()) {
                val o = arreglo.getJSONObject(i)
                sugeridos.add(
                    ProductoSugerido(
                        codigo = o.optString("codigo"),
                        nombre = o.optString("nombre"),
                        valor = o.optDouble("valor", 0.0),
                        estadoStock = o.optString("estadoStock", "DISPONIBLE"),
                        cantidad = o.optInt("cantidad", 1)
                    )
                )
            }
            ResultadoIA.Exito(RespuestaIA(json.optString("respuesta"), sugeridos))
        } catch (e: java.net.SocketTimeoutException) {
            ResultadoIA.Error("El asistente tardó demasiado en responder. Intenta de nuevo.")
        } catch (e: Exception) {
            ResultadoIA.Error(
                "No pude conectar con el servidor. Revisa que el backend esté encendido " +
                        "y que la URL del servidor sea correcta."
            )
        } finally {
            conexion?.disconnect()
        }
    }
}