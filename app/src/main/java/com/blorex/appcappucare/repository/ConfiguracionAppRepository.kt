package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.blorex.appcappucare.room.AppRoomDatabase
import com.blorex.appcappucare.room.entities.ConfiguracionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Gamas de colores predefinidas para la personalización de la aplicación por el usuario de TI (`blorex`).
 */
enum class GamaColorApp {
    /** Verde Médico y Blanco (Predeterminado - #2E7D32). */
    VERDE_SALUD,

    /** Rojo Pálido Médico (#C65353). */
    ROJO_PALIDO,

    /** Azul Claro / Celeste Hospitalario (#0288D1). */
    AZUL_HOSPITALARIO,

    /** Azul Marino / Corporativo (#1565C0). */
    AZUL_CORPORATIVO,

    /** Púrpura / Morado Salud (#673AB7). */
    PURPURA_SALUD
}

/**
 * Repositorio de configuración y personalización visual de la aplicación en Jetpack Room (Logo, Portadas y Temas).
 */
object ConfiguracionAppRepository {

    private var roomDatabase: AppRoomDatabase? = null

    /** URI o ruta de la foto de logo personalizada elegida por TI. */
    var logoAppUri by mutableStateOf("")

    /** URI o ruta de la imagen de portada/banner personalizada elegida por TI. */
    var portadaAppUri by mutableStateOf("")

    /** Gama de colores activa de la aplicación. */
    var gamaColorSeleccionada by mutableStateOf(GamaColorApp.VERDE_SALUD)

    /** Inicializa la conexión con Room para leer la configuración de logo y gama de colores. */
    fun inicializar(context: Context) {
        if (roomDatabase == null) {
            roomDatabase = AppRoomDatabase.getDatabase(context.applicationContext)
            cargarDesdeDb()
        }
    }

    /** Carga las preferencias de logo y tema guardadas en la base de datos de Room. */
    fun cargarDesdeDb() {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                val entidades = db.configuracionDao().obtenerConfiguracion()
                var logo = ""
                var gamaStr = "VERDE_SALUD"
                for (c in entidades) {
                    if (c.clave == "logoAppUri") logo = c.valor
                    if (c.clave == "gamaColor") gamaStr = c.valor
                }

                CoroutineScope(Dispatchers.Main).launch {
                    logoAppUri = logo
                    gamaColorSeleccionada = try {
                        GamaColorApp.valueOf(gamaStr)
                    } catch (e: Exception) {
                        GamaColorApp.VERDE_SALUD
                    }
                }
            }
        }
    }

    /** Retorna la ruta de la portada vinculada según la gama de colores seleccionada o personalizada por TI. */
    fun obtenerPortadaActual(): String {
        if (portadaAppUri.isNotBlank()) return portadaAppUri
        return when (gamaColorSeleccionada) {
            GamaColorApp.VERDE_SALUD -> "asset://portadas/celeste.jpeg"
            GamaColorApp.ROJO_PALIDO -> "asset://portadas/rojo.jpeg"
            GamaColorApp.AZUL_HOSPITALARIO -> "asset://portadas/celeste.jpeg"
            GamaColorApp.AZUL_CORPORATIVO -> "asset://portadas/azul marino.jpeg"
            GamaColorApp.PURPURA_SALUD -> "asset://portadas/morado.jpeg"
        }
    }

    /** Actualiza y persiste la URI del logo corporativo en Room. */
    fun actualizarLogo(nuevaUri: String) {
        logoAppUri = nuevaUri
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.configuracionDao().guardarConfiguracion(ConfiguracionEntity("logoAppUri", logoAppUri))
                db.configuracionDao().guardarConfiguracion(ConfiguracionEntity("gamaColor", gamaColorSeleccionada.name))
            }
        }
    }

    /** Actualiza y persiste la gama de colores seleccionada por TI en Room. */
    fun actualizarGamaColor(nuevaGama: GamaColorApp) {
        gamaColorSeleccionada = nuevaGama
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.configuracionDao().guardarConfiguracion(ConfiguracionEntity("logoAppUri", logoAppUri))
                db.configuracionDao().guardarConfiguracion(ConfiguracionEntity("gamaColor", gamaColorSeleccionada.name))
            }
        }
    }
}
