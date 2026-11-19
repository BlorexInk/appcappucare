package com.blorex.appcappucare.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto

/**
 * Entidad de Room que representa la tabla `productos` en la base de datos local SQLite.
 */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val valor: Double = 0.0,
    val cantidadDisponible: Int = 0,
    val unidadMedida: String = "un",
    val nivelMinimo: Int = 10,
    val estadoStock: String = EstadoStock.DISPONIBLE.name,
    val imagenes: String = "" // Guardado como lista delimitada por ';'
) {
    /** Convierte esta entidad de Room al modelo de dominio [Producto]. */
    fun toDomainModel(): Producto {
        val estado = try {
            EstadoStock.valueOf(estadoStock)
        } catch (e: Exception) {
            EstadoStock.DISPONIBLE
        }
        val listImgs = if (imagenes.isBlank()) emptyList() else imagenes.split(";")
        return Producto(
            codigo = codigo,
            nombre = nombre,
            descripcion = descripcion,
            valor = valor,
            cantidadDisponible = cantidadDisponible,
            unidadMedida = unidadMedida,
            nivelMinimo = nivelMinimo,
            estadoStock = estado,
            imagenes = listImgs
        )
    }

    companion object {
        /** Mapea un [Producto] del dominio a una [ProductoEntity] de Room. */
        fun fromDomainModel(p: Producto): ProductoEntity {
            return ProductoEntity(
                codigo = p.codigo,
                nombre = p.nombre,
                descripcion = p.descripcion,
                valor = p.valor,
                cantidadDisponible = p.cantidadDisponible,
                unidadMedida = p.unidadMedida,
                nivelMinimo = p.nivelMinimo,
                estadoStock = p.estadoStock.name,
                imagenes = p.imagenes.joinToString(";")
            )
        }
    }
}
