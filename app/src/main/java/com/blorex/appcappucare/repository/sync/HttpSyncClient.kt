package com.blorex.appcappucare.repository.sync

import android.content.Context
import android.net.Uri
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.GamaColorApp
import com.blorex.appcappucare.repository.ProductosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Resultado estructurado del proceso de sincronización con el servidor Backend.
 *
 * @property exitoso `true` si la sincronización se completó sin errores; `false` de lo contrario.
 * @property mensaje Mensaje descriptivo para el usuario.
 * @property totalSincronizados Número total de elementos sincronizados en el servidor.
 */
data class ResultadoSync(
    val exitoso: Boolean,
    val mensaje: String,
    val totalSincronizados: Int = 0
)

/**
 * Cliente HTTP nativo para la transmisión de archivos binarios de imágenes (Multipart)
 * y sincronización bidireccional JSON de productos, usuarios y configuración entre la app Android y el Servidor Backend REST API.
 */
object HttpSyncClient {

    suspend fun eliminarProductoServidor(
        baseUrl: String,
        codigo: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "${baseUrl.trimEnd('/')}/api/productos/${java.net.URLEncoder.encode(codigo, "UTF-8")}"
            )

            val conexion = url.openConnection() as HttpURLConnection

            try {
                conexion.requestMethod = "DELETE"
                conexion.connectTimeout = 10000
                conexion.readTimeout = 10000

                conexion.responseCode in 200..299
            } finally {
                conexion.disconnect()
            }
        } catch (e: Exception) {
            false
        }
    }


    /**
     * Procesa la subida de fotos de la galería al servidor Backend y sincroniza inventario, usuarios y configuración global de marca.
     *
     * @param context Contexto de la aplicación.
     * @param baseUrl URL base del servidor Backend (ej: "http://10.0.2.2:3000" para Emulador o IP de red local).
     * @return [ResultadoSync] con el resultado de la operación.
     */
    suspend fun procesarSincronizacionCompleta(
        context: Context,
        baseUrl: String = "http://10.0.2.2:3000"
    ): ResultadoSync = withContext(Dispatchers.IO) {
        try {
            val todosLosProductos = ProductosRepository.getProductosFicticios()
            val todosLosUsuarios = AuthRepository.obtenerUsuarios()
            val productosProcesados = mutableListOf<Producto>()

            // 1. Subir imágenes locales (content://) al servidor Backend
            for (prod in todosLosProductos) {
                val imagenesActualizadas = mutableListOf<String>()

                for (imgUriStr in prod.imagenes) {
                    if (imgUriStr.startsWith("content://") || imgUriStr.startsWith("file://")) {
                        val serverUrlFoto = subirImagenBinariaServidor(context, baseUrl, imgUriStr)
                        if (serverUrlFoto != null) {
                            imagenesActualizadas.add(serverUrlFoto)
                        } else {
                            imagenesActualizadas.add(imgUriStr)
                        }
                    } else {
                        imagenesActualizadas.add(imgUriStr)
                    }
                }

                val prodActualizado = prod.copy(imagenes = imagenesActualizadas)
                productosProcesados.add(prodActualizado)
                ProductosRepository.actualizarProducto(prodActualizado)
            }

            // 2. Transmitir catálogo completo de productos y usuarios en JSON
            val jsonPayload = JSONObject()
            val arrayProdsJson = JSONArray()

            for (p in productosProcesados) {
                val itemObj = JSONObject().apply {
                    put("codigo", p.codigo)
                    put("nombre", p.nombre)
                    put("descripcion", p.descripcion)
                    put("valor", p.valor)
                    put("cantidadDisponible", p.cantidadDisponible)
                    put("unidadMedida", p.unidadMedida)
                    put("nivelMinimo", p.nivelMinimo)
                    put("estadoStock", p.estadoStock.name)

                    val imgsArr = JSONArray()
                    p.imagenes.forEach { imgsArr.put(it) }
                    put("imagenes", imgsArr)
                }
                arrayProdsJson.put(itemObj)
            }
            jsonPayload.put("productos", arrayProdsJson)

            // 3. Array de Usuarios
            val arrayUsersJson = JSONArray()
            for (u in todosLosUsuarios) {
                val uObj = JSONObject().apply {
                    put("username", u.username)
                    put("password", u.password)
                    put("nombreCompleto", u.nombreCompleto)
                    put("rol", u.rol)
                    put("categoriaCargo", u.categoriaCargo)
                    put("email", u.email)
                    put("telefono", u.telefono)
                    put("direccion", u.direccion)
                    put("ubicacionBodega", u.ubicacionBodega)
                    put("fotoPerfilUrl", u.fotoPerfilUrl)
                    put("fechaRegistro", u.fechaRegistro)
                    put("horaInicioSesion", u.horaInicioSesion)
                }
                arrayUsersJson.put(uObj)
            }
            jsonPayload.put("usuarios", arrayUsersJson)

            // 4. Configuración de Marca (Logo y Gama de Colores)
            val configObj = JSONObject().apply {
                put("logoAppUri", ConfiguracionAppRepository.logoAppUri)
                put("gamaColor", ConfiguracionAppRepository.gamaColorSeleccionada.name)
            }
            jsonPayload.put("configuracion", configObj)

            // 5. Realizar la petición POST HTTP a /api/sync
            val syncUrl = URL("$baseUrl/api/sync")
            val conn = (syncUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            DataOutputStream(conn.outputStream).use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(responseText)

                // 6. Sincronizar usuarios devueltos por el servidor
                if (jsonResponse.has("usuarios")) {
                    val arrayRespuestaUsers = jsonResponse.getJSONArray("usuarios")
                    for (i in 0 until arrayRespuestaUsers.length()) {
                        val item = arrayRespuestaUsers.getJSONObject(i)
                        val emailAjustado = item.optString("email", "blorexink@gmail.com").ifBlank { "blorexink@gmail.com" }
                        val uServidor = Usuario(
                            username = item.getString("username"),
                            password = item.optString("password", "1234"),
                            nombreCompleto = item.optString("nombreCompleto", item.getString("username")),
                            rol = item.optString("rol", "Vendedor"),
                            categoriaCargo = item.optString("categoriaCargo", "Ventas y Mostrador"),
                            email = emailAjustado,
                            telefono = item.optString("telefono", ""),
                            direccion = item.optString("direccion", ""),
                            ubicacionBodega = item.optString("ubicacionBodega", "Bodega Central XXXX"),
                            fotoPerfilUrl = item.optString("fotoPerfilUrl", ""),
                            fechaRegistro = item.optString("fechaRegistro", "20/10/2026"),
                            horaInicioSesion = item.optString("horaInicioSesion", "Sesión Inactiva")
                        )
                        AuthRepository.guardarOActualizarUsuario(uServidor)
                    }
                }

                // 7. Descargar y aplicar la configuración global de colores y marca fijada por TI en el HILO PRINCIPAL (Main Thread)
                if (jsonResponse.has("configuracion")) {
                    val cfgRes = jsonResponse.getJSONObject("configuracion")
                    val logoRemoto = if (cfgRes.has("logoAppUri")) cfgRes.getString("logoAppUri") else null
                    val gamaStrRemota = if (cfgRes.has("gamaColor")) cfgRes.getString("gamaColor") else null

                    withContext(Dispatchers.Main) {
                        if (!logoRemoto.isNullOrBlank()) {
                            ConfiguracionAppRepository.actualizarLogo(logoRemoto)
                        }
                        if (!gamaStrRemota.isNullOrBlank()) {
                            try {
                                val gamaRemota = GamaColorApp.valueOf(gamaStrRemota)
                                ConfiguracionAppRepository.actualizarGamaColor(gamaRemota)
                            } catch (e: Exception) {
                                // Mantiene gama actual
                            }
                        }
                    }
                }

                val totalP = jsonResponse.optInt("totalProductos", productosProcesados.size)
                val totalU = jsonResponse.optInt("totalUsuarios", todosLosUsuarios.size)

                ResultadoSync(
                    exitoso = true,
                    mensaje = "¡Sincronización exitosa! $totalP productos y $totalU usuarios sincronizados con el servidor Backend.",
                    totalSincronizados = totalP + totalU
                )
            } else {
                ResultadoSync(
                    exitoso = false,
                    mensaje = "Error del servidor Backend (Código HTTP $responseCode). Verifica que el servidor /backend/server.js esté ejecutándose."
                )
            }
        } catch (e: Exception) {
            ResultadoSync(
                exitoso = false,
                mensaje = "No se pudo conectar con el Servidor Backend (${e.localizedMessage ?: "Conexión rechazada"}). Asegúrate de ejecutar 'npm start' en la carpeta /backend/."
            )
        }
    }

    /** Transmite una foto local al endpoint Multipart /api/upload del servidor. */
    private fun subirImagenBinariaServidor(
        context: Context,
        baseUrl: String,
        uriString: String
    ): String? {
        val boundary = "Boundary-${UUID.randomUUID()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val url = URL("$baseUrl/api/upload")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                doOutput = true
                doInput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            DataOutputStream(conn.outputStream).use { dos ->
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"imagen\"; filename=\"foto_galeria.jpg\"$lineEnd")
                dos.writeBytes("Content-Type: image/jpeg$lineEnd$lineEnd")

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    dos.write(buffer, 0, bytesRead)
                }
                dos.writeBytes(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                dos.flush()
            }

            inputStream.close()

            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(response)
                return if (json.has("url")) json.getString("url") else null
            }
        } catch (e: Exception) {
            // Silencioso
        }
        return null
    }

    /** Envía la configuración de marca (logo y gama de colores) directamente al Servidor Backend. */
    suspend fun enviarConfiguracionAlServidor(baseUrl: String = "http://10.0.2.2:3000"): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/configuracion")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val json = JSONObject().apply {
                put("logoAppUri", ConfiguracionAppRepository.logoAppUri)
                put("gamaColor", ConfiguracionAppRepository.gamaColorSeleccionada.name)
            }

            DataOutputStream(conn.outputStream).use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            return@withContext (conn.responseCode in 200..299)
        } catch (e: Exception) {
            return@withContext false
        }
    }
}
