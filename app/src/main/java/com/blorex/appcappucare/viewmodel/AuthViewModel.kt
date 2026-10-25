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

    /** Mensaje de error general de inicio de sesión (credenciales incorrectas). */
    var errorLogin by mutableStateOf<String?>(null)

    /** Error de validación del campo usuario. */
    var errorUsuario by mutableStateOf<String?>(null)
        private set

    /** Error de validación del campo contraseña. */
    var errorPassword by mutableStateOf<String?>(null)
        private set

    /** Error de validación del campo de recuperación de clave. */
    var errorRecuperacion by mutableStateOf<String?>(null)
        private set

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
        errorLogin = null
        errorUsuario = validarUsuario(usernameInput)
        errorPassword = validarPassword(passwordInput)
        if (errorUsuario != null || errorPassword != null) {
            return false
        }

        val usuario = AuthRepository.autenticar(usernameInput, passwordInput)
        if (usuario != null) {
            usuarioLogueado = usuario
            usernameInput = ""
            passwordInput = ""
            return true
        }
        errorLogin = "Usuario o contraseña incorrectos"
        return false
    }

    private fun validarUsuario(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu usuario"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    private fun validarPassword(valor: String): String? = when {
        valor.isEmpty() -> "Ingresa tu contraseña"
        valor.length < 4 -> "Debe tener al menos 4 caracteres"
        else -> null
    }

    /** Actualiza el usuario ingresado y limpia sus errores. */
    fun onUsuarioChange(valor: String) {
        usernameInput = valor
        errorUsuario = null
        errorLogin = null
    }

    /** Actualiza la contraseña ingresada y limpia sus errores. */
    fun onPasswordChange(valor: String) {
        passwordInput = valor
        errorPassword = null
        errorLogin = null
    }

    /** Actualiza el texto de recuperación y limpia sus mensajes. */
    fun onRecuperacionChange(valor: String) {
        recoveryQueryInput = valor
        errorRecuperacion = null
        mensajeRecuperacion = null
    }

    /** Cierra la sesión activa y limpia los datos en memoria. */
    fun cerrarSesion() {
        usuarioLogueado = null
        errorLogin = null
        errorUsuario = null
        errorPassword = null
        usernameInput = ""
        passwordInput = ""
    }

    /** Abre el cuadro de diálogo para recuperación de contraseña. */
    fun abrirDialogoRecuperar() {
        recoveryQueryInput = ""
        mensajeRecuperacion = null
        errorRecuperacion = null
        mostrarDialogoRecuperar = true
    }

    /** Cierra el diálogo de recuperación de contraseña. */
    fun cerrarDialogoRecuperar() {
        mostrarDialogoRecuperar = false
        recoveryQueryInput = ""
        mensajeRecuperacion = null
        errorRecuperacion = null
    }

    /** Procesa la solicitud de recuperación de contraseña para el correo o usuario ingresado. */
    fun enviarSolicitudRecuperacion() {
        mensajeRecuperacion = null
        val consulta = recoveryQueryInput.trim()
        if (consulta.isEmpty()) {
            errorRecuperacion = "Ingresa tu usuario o correo electrónico"
            return
        }
        if (consulta.contains("@") && !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(consulta)) {
            errorRecuperacion = "El correo no tiene un formato válido"
            return
        }

        val usuario = AuthRepository.buscarPorUsernameOEmail(consulta)
        if (usuario != null) {
            errorRecuperacion = null
            mensajeRecuperacion = "Se enviaron los pasos para recuperar la contraseña de '${usuario.username}' a ${usuario.email}."
        } else {
            errorRecuperacion = "No se encontró ningún usuario con ese nombre o correo"
        }
    }
}
