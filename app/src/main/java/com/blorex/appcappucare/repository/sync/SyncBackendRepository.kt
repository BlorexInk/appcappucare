package com.blorex.appcappucare.repository.sync

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Modos de conexión y origen de datos soportados por el sistema AppCappuCare.
 */
enum class ModoConexion {
    /** Modo Offline: Lectura y escritura directa en la base de datos local Room / SQLite. */
    OFFLINE_ROOM,

    /** Modo Online: Sincronización automática de cambios con el servidor Backend REST / Firebase. */
    ONLINE_BACKEND
}

/**
 * Repositorio y gestor de sincronización entre la base de datos local (Room/SQLite)
 * y la base de datos remota del servidor Backend.
 */
object SyncBackendRepository {

    /** Modo de operación actual (Offline Room por defecto). */
    var modoConexion by mutableStateOf(ModoConexion.OFFLINE_ROOM)

    /** URL configurable del servidor Backend (Por defecto 10.0.2.2:3000 para Android Emulator). */
    var servidorUrlInput by mutableStateOf("http://10.0.2.2:3000")

    /** Indica si hay un proceso de sincronización en ejecución. */
    var estaSincronizando by mutableStateOf(false)
        private set

    /** Registro de fecha y resultado del último intento de sincronización. */
    var ultimoMensajeSincronizacion by mutableStateOf<String?>(null)

    /**
     * Ejecuta el proceso de sincronización real entre la base de datos local y el servidor Backend.
     * Transmite las imágenes de la galería del teléfono y el archivo de base de datos en JSON.
     *
     * @param context Contexto de la aplicación.
     * @param scope Scope para corrutinas en segundo plano.
     */
    fun sincronizarConServidorBackend(context: Context, scope: CoroutineScope) {
        if (estaSincronizando) return

        scope.launch {
            estaSincronizando = true
            ultimoMensajeSincronizacion = "Conectando y transmitiendo imágenes y productos al servidor..."

            val resultado = HttpSyncClient.procesarSincronizacionCompleta(
                context = context,
                baseUrl = servidorUrlInput.trim().removeSuffix("/")
            )

            ultimoMensajeSincronizacion = resultado.mensaje
            estaSincronizando = false
        }
    }
}
