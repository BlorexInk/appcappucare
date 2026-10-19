package com.blorex.appcappucare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.ui.theme.OpcionTema

/**
 * ViewModel que gestiona la autenticación de usuarios, inicio/cierre de sesión, recuperación de contraseña y preferencias de tema visual.
 */
class AuthViewModel : ViewModel() {

    /** Usuario actualmente autenticado en el sistema (`null` si no hay sesión activa). */
    var usuarioLogueado by mutableStateOf<Usuario?>(null)
        private set

    /** Estado del campo de texto de usuario. */
    var usernameInput by mutableStateOf("")

    /** Estado del campo de texto de contraseña. */
    var passwordInput by mutableStateOf("")

    /** Mensaje de error de inicio de sesión. */
    var errorLogin by mutableStateOf<String?>(null)

    /** Estado de visibilidad del diálogo de recuperación de clave. */
    var mostrarDialogoRecuperar by mutableStateOf(false)
        private set

    /** Campo de búsqueda para recuperación (Usuario o Email). */
    var recoveryQueryInput by mutableStateOf("")

    /** Mensaje de respuesta del proceso de recuperación de contraseña. */
    var mensajeRecuperacion by mutableStateOf<String?>(null)

    /** Opción de configuración de notificaciones de stock activas. */
    var notificacionesActivas by mutableStateOf(true)

    /** Opción seleccionada para el tema visual de la app (SISTEMA, CLARO, OSCURO). */
    var opcionTema by mutableStateOf(OpcionTema.SISTEMA)

    /**
     * Inicia sesión con las credenciales ingresadas.
     *
     * @return `true` si el acceso fue exitoso; `false` si las credenciales son incorrectas.
     */
    fun iniciarSesion(): Boolean {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            errorLogin = "Por favor, completa todos los campos"
            return false
        }

        val usuario = AuthRepository.autenticar(usernameInput, passwordInput)
        if (usuario != null) {
            usuarioLogueado = usuario
            errorLogin = null
            usernameInput = ""
            passwordInput = ""
            return true
        } else {
            errorLogin = "Usuario o contraseña incorrectos"
            return false
        }
    }

    /** Cierra la sesión activa y limpia los datos en memoria. */
    fun cerrarSesion() {
        usuarioLogueado = null
        errorLogin = null
        usernameInput = ""
        passwordInput = ""
    }

    /** Abre el cuadro de diálogo para recuperación de contraseña. */
    fun abrirDialogoRecuperar() {
        recoveryQueryInput = ""
        mensajeRecuperacion = null
        mostrarDialogoRecuperar = true
    }

    /** Cierra el diálogo de recuperación de contraseña. */
    fun cerrarDialogoRecuperar() {
        mostrarDialogoRecuperar = false
        recoveryQueryInput = ""
        mensajeRecuperacion = null
    }

    /** Procesa la solicitud de recuperación de contraseña para el correo o usuario ingresado. */
    fun enviarSolicitudRecuperacion() {
        if (recoveryQueryInput.isBlank()) {
            mensajeRecuperacion = "Ingresa tu usuario o correo electrónico"
            return
        }

        val usuario = AuthRepository.buscarPorUsernameOEmail(recoveryQueryInput)
        if (usuario != null) {
            mensajeRecuperacion = "¡Instrucciones enviadas! Se enviaron los pasos para recuperar la contraseña de '${usuario.username}' a ${usuario.email}."
        } else {
            mensajeRecuperacion = "No se encontró ningún usuario con ese nombre o correo."
        }
    }
}
