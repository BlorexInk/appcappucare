package com.blorex.appcappucare.model

/**
 * Modelo de datos que representa un usuario del sistema AppCappuCare.
 *
 * @property username Nombre de usuario único para inicio de sesión (ej: "blorex").
 * @property password Contraseña de acceso.
 * @property nombreCompleto Nombre y apellidos del usuario.
 * @property rol Cargo o nivel de acceso dentro del sistema (ej: "Administrador", "Gerente").
 * @property email Correo electrónico de contacto para recuperación de cuenta.
 */
data class Usuario(
    val username: String,
    val password: String,
    val nombreCompleto: String,
    val rol: String,
    val email: String
)
