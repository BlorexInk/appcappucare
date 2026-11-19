package com.blorex.appcappucare.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.room.AppRoomDatabase
import com.blorex.appcappucare.room.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio encargado del control de usuarios, roles, permisos y autenticación en Jetpack Room.
 */
object AuthRepository {

    private var roomDatabase: AppRoomDatabase? = null
    private val listaUsuarios = mutableStateListOf<Usuario>()

    /** Usuarios habilitados por defecto en el sistema. */
    val usuariosPorDefecto = listOf(
        Usuario("blorex", "1234", "Carlos Blorex", "TI", "Sistemas / TI", "blorexink@gmail.com", "+56 9 1234 5678", "Av. Principal 100", "Bodega Central XXXX"),
        Usuario("admin", "admin123", "Ana Martínez", "Administrador", "Administración General", "blorexink@gmail.com", "+56 9 8765 4321", "Calle Centro 250", "Bodega Central XXXX"),
        Usuario("gerente", "gerente123", "Roberto Silva", "Gerente de Tienda", "Gerencia de Tienda", "blorexink@gmail.com", "+56 9 5555 6666", "Av. Los Leones 50", "Bodega Central XXXX"),
        Usuario("vendedor", "pass123", "Luis Gómez", "Vendedor", "Ventas y Mostrador", "blorexink@gmail.com", "+56 9 4444 3333", "Pasaje Sol 12", "Bodega Central XXXX")
    )

    /**
     * Inicializa el repositorio con la base de datos de Room y carga los usuarios persistidos.
     */
    fun inicializar(context: Context) {
        if (roomDatabase == null) {
            roomDatabase = AppRoomDatabase.getDatabase(context.applicationContext)
            cargarDesdeDb()
        }
    }

    /** Recarga los usuarios desde la base de datos local Room. */
    fun cargarDesdeDb() {
        val db = roomDatabase
        if (db != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val entidades = db.usuarioDao().obtenerTodosUsuarios()
                val lista = entidades.map { it.toDomainModel() }
                CoroutineScope(Dispatchers.Main).launch {
                    listaUsuarios.clear()
                    if (lista.isEmpty()) {
                        listaUsuarios.addAll(usuariosPorDefecto)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.usuarioDao().guardarTodos(usuariosPorDefecto.map { UsuarioEntity.fromDomainModel(it) })
                        }
                    } else {
                        for (u in lista) {
                            val emailAjustado = if (u.email.isBlank() || u.email.contains("appcappucare.com")) "blorexink@gmail.com" else u.email
                            listaUsuarios.add(u.copy(email = emailAjustado))
                        }
                    }
                }
            }
        } else {
            if (listaUsuarios.isEmpty()) {
                listaUsuarios.addAll(usuariosPorDefecto)
            }
        }
    }

    /** Retorna la lista reactiva de usuarios registrados. */
    fun obtenerUsuarios(): List<Usuario> {
        if (listaUsuarios.isEmpty()) {
            listaUsuarios.addAll(usuariosPorDefecto)
        }
        return listaUsuarios
    }

    /**
     * Valida credenciales e inscribe el horario exacto de inicio de sesión del usuario.
     *
     * @param usernameInput Nombre de usuario ingresado.
     * @param passwordInput Contraseña ingresada.
     * @return Objeto [Usuario] autenticado con horario de sesión registrado.
     */
    fun autenticar(usernameInput: String, passwordInput: String): Usuario? {
        val inputUser = usernameInput.trim()
        val inputPass = passwordInput.trim()

        if (listaUsuarios.isEmpty()) {
            listaUsuarios.addAll(usuariosPorDefecto)
        }

        var usuarioEncontrado = listaUsuarios.find {
            it.username.equals(inputUser, ignoreCase = true) && it.password == inputPass
        }

        if (usuarioEncontrado == null) {
            usuarioEncontrado = usuariosPorDefecto.find {
                it.username.equals(inputUser, ignoreCase = true) && it.password == inputPass
            }
        }

        if (usuarioEncontrado != null) {
            val horaActual = SimpleDateFormat("HH:mm 'hrs' - dd/MM/yyyy", Locale.getDefault()).format(Date())
            val usuarioConSesion = usuarioEncontrado.copy(horaInicioSesion = horaActual)

            guardarOActualizarUsuario(usuarioConSesion)
            return usuarioConSesion
        }
        return null
    }

    /** Busca un usuario por username o correo. */
    fun buscarPorUsernameOEmail(query: String): Usuario? {
        val q = query.trim()
        if (listaUsuarios.isEmpty()) {
            listaUsuarios.addAll(usuariosPorDefecto)
        }
        return listaUsuarios.find {
            it.username.equals(q, ignoreCase = true) || it.email.equals(q, ignoreCase = true)
        }
    }

    /** Registra o actualiza un usuario en la BD de Room. */
    fun guardarOActualizarUsuario(usuario: Usuario): Boolean {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.usuarioDao().guardarOActualizarUsuario(UsuarioEntity.fromDomainModel(usuario))
            }
        }

        val idx = listaUsuarios.indexOfFirst { it.username.equals(usuario.username, ignoreCase = true) }
        if (idx != -1) {
            listaUsuarios[idx] = usuario
        } else {
            listaUsuarios.add(usuario)
        }
        return true
    }

    /** Elimina un usuario de la BD de Room. */
    fun eliminarUsuario(username: String): Boolean {
        roomDatabase?.let { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.usuarioDao().eliminarUsuario(username)
            }
        }
        return listaUsuarios.removeIf { it.username.equals(username, ignoreCase = true) }
    }
}
