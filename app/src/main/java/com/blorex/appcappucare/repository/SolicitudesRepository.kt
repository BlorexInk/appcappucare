package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.SolicitudModificacion
import com.blorex.appcappucare.room.AppRoomDatabase
import com.blorex.appcappucare.room.entities.SolicitudEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repositorio reactivo y persistente en Jetpack Room para la recepción y gestión de solicitudes de modificación enviadas a TI (`blorex`).
 */
object SolicitudesRepository {

    private var roomDatabase: AppRoomDatabase? = null
    private val listaSolicitudes = mutableStateListOf<SolicitudModificacion>()

    /** Inicializa la conexión con Room y carga las solicitudes persistidas. */
    fun inicializar(context: Context) {
        if (roomDatabase == null) {
            roomDatabase = AppRoomDatabase.getDatabase(context.applicationContext)
            cargarDesdeDb()
        }
    }

    /** Carga las solicitudes registradas desde Room. */
    fun cargarDesdeDb() {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                val entidades = db.solicitudDao().obtenerTodasSolicitudes()
                val lista = entidades.map { it.toDomainModel() }
                CoroutineScope(Dispatchers.Main).launch {
                    listaSolicitudes.clear()
                    listaSolicitudes.addAll(lista)
                }
            }
        }
    }

    /** Retorna la lista reactiva de solicitudes registradas. */
    fun obtenerSolicitudes(): List<SolicitudModificacion> = listaSolicitudes

    /** Retorna el número de solicitudes en estado PENDIENTE. */
    fun conteoPendientes(): Int {
        return listaSolicitudes.count { it.estado.equals("PENDIENTE", ignoreCase = true) }
    }

    /** Registra una nueva solicitud de cambio enviada por un trabajador en Room. */
    fun crearSolicitud(s: SolicitudModificacion): Boolean {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.solicitudDao().guardarOActualizarSolicitud(SolicitudEntity.fromDomainModel(s))
            }
        }
        listaSolicitudes.add(0, s)
        return true
    }

    /** Marca una solicitud como ATENDIDA en Room. */
    fun marcarComoAtendido(id: String): Boolean {
        val idx = listaSolicitudes.indexOfFirst { it.id == id }
        if (idx != -1) {
            val atendida = listaSolicitudes[idx].copy(estado = "ATENDIDO")
            roomDatabase?.let { db ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.solicitudDao().guardarOActualizarSolicitud(SolicitudEntity.fromDomainModel(atendida))
                }
            }
            listaSolicitudes[idx] = atendida
            return true
        }
        return false
    }
}
