package com.blorex.appcappucare.repository

import com.blorex.appcappucare.model.Usuario

/**
 * Repositorio encargado del control de usuarios y la autenticación del sistema AppCappuCare.
 */
object AuthRepository {

    /** Lista estática de usuarios habilitados para demostración y acceso al sistema. */
    private val usuarios = listOf(
        Usuario("blorex", "1234", "Carlos Blorex", "Administrador", "carlos@appcappucare.com"),
        Usuario("admin", "admin123", "Ana Martínez", "Gerente de Inventario", "ana.admin@appcappucare.com"),
        Usuario("vendedor", "pass123", "Luis Gómez", "Vendedor Mostrador", "luis.ventas@appcappucare.com")
    )

    /**
     * Valida las credenciales ingresadas por un usuario en la pantalla de Login.
     *
     * @param usernameInput Nombre de usuario ingresado.
     * @param passwordInput Contraseña ingresada.
     * @return Objeto [Usuario] si las credenciales son correctas; `null` si son inválidas.
     */
    fun autenticar(usernameInput: String, passwordInput: String): Usuario? {
        return usuarios.find {
            it.username.equals(usernameInput.trim(), ignoreCase = true) && it.password == passwordInput
        }
    }

    /**
     * Busca un usuario por su nombre de usuario o correo para el proceso de recuperación de clave.
     *
     * @param query Nombre de usuario o correo electrónico.
     * @return Objeto [Usuario] si se encuentra una coincidencia; `null` en caso contrario.
     */
    fun buscarPorUsernameOEmail(query: String): Usuario? {
        val q = query.trim()
        return usuarios.find {
            it.username.equals(q, ignoreCase = true) || it.email.equals(q, ignoreCase = true)
        }
    }
}
