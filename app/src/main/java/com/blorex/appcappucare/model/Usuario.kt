package com.blorex.appcappucare.model

/**
 * Modelo de datos que representa un usuario/trabajador del sistema AppCappuCare.
 *
 * @property username Nombre de usuario único para inicio de sesión (ej: "blorex").
 * @property password Contraseña de acceso.
 * @property nombreCompleto Nombre y apellidos del trabajador.
 * @property rol Nivel de permisos ("TI", "Administrador", "Gerente de Tienda", "Vendedor").
 * @property categoriaCargo Clasificación del cargo ("Sistemas / TI", "Administración General", "Gerencia de Tienda", "Ventas y Mostrador").
 * @property email Correo electrónico de contacto.
 * @property telefono Número de teléfono móvil o de contacto.
 * @property direccion Dirección residencial o comercial.
 * @property ubicacionBodega Ubicación asignada de bodega (por defecto "Bodega Central XXXX").
 * @property fotoPerfilUrl URI o ruta de la foto de perfil del usuario.
 * @property fechaRegistro Fecha de creación de la cuenta de usuario.
 * @property horaInicioSesion Registro con la fecha y hora exacta de la última activación/inicio de sesión.
 */
data class Usuario(
    val username: String,
    val password: String,
    val nombreCompleto: String,
    val rol: String,
    val categoriaCargo: String = "Ventas y Mostrador",
    val email: String,
    val telefono: String = "+56 9 1234 5678",
    val direccion: String = "Av. Principal 100",
    val ubicacionBodega: String = "Bodega Central XXXX",
    val fotoPerfilUrl: String = "",
    val fechaRegistro: String = "20/10/2026",
    val horaInicioSesion: String = "Sesión Inactiva"
)
