package com.blorex.appcappucare.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blorex.appcappucare.model.Usuario

/**
 * Entidad de Room que representa la tabla `usuarios` en la base de datos local SQLite.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val username: String,
    val password: String,
    val nombreCompleto: String,
    val rol: String,
    val categoriaCargo: String = "Ventas y Mostrador",
    val email: String = "blorexink@gmail.com",
    val telefono: String = "+56 9 1234 5678",
    val direccion: String = "Av. Principal 100",
    val ubicacionBodega: String = "Bodega Central XXXX",
    val fotoPerfilUrl: String = "",
    val fechaRegistro: String = "20/10/2026",
    val horaInicioSesion: String = "Sesión Inactiva"
) {
    /** Convierte esta entidad de Room al modelo de dominio [Usuario]. */
    fun toDomainModel(): Usuario {
        return Usuario(
            username = username,
            password = password,
            nombreCompleto = nombreCompleto,
            rol = rol,
            categoriaCargo = categoriaCargo,
            email = email,
            telefono = telefono,
            direccion = direccion,
            ubicacionBodega = ubicacionBodega,
            fotoPerfilUrl = fotoPerfilUrl,
            fechaRegistro = fechaRegistro,
            horaInicioSesion = horaInicioSesion
        )
    }

    companion object {
        /** Mapea un [Usuario] del dominio a una [UsuarioEntity] de Room. */
        fun fromDomainModel(u: Usuario): UsuarioEntity {
            return UsuarioEntity(
                username = u.username,
                password = u.password,
                nombreCompleto = u.nombreCompleto,
                rol = u.rol,
                categoriaCargo = u.categoriaCargo,
                email = u.email,
                telefono = u.telefono,
                direccion = u.direccion,
                ubicacionBodega = u.ubicacionBodega,
                fotoPerfilUrl = u.fotoPerfilUrl,
                fechaRegistro = u.fechaRegistro,
                horaInicioSesion = u.horaInicioSesion
            )
        }
    }
}
