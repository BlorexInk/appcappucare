package com.blorex.appcappucare.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto

/**
 * Entidad de tabla Room para persistencia de datos en la base de datos local SQLite.
 */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val descripcion: String,
    val valor: Double,
    val cantidadDisponible: Int,
    val unidadMedida: String,
    val nivelMinimo: Int,
    val estadoStock: String,
    val imagenesJson: String
) {
    /** Convierte la entidad de Room al modelo de dominio [Producto]. */
    fun toModel(): Producto {
        val listImagenes = if (imagenesJson.isBlank()) emptyList() else imagenesJson.split(";")
        val estado = try {
            EstadoStock.valueOf(estadoStock)
        } catch (e: Exception) {
            EstadoStock.DISPONIBLE
        }
        return Producto(
            codigo = codigo,
            nombre = nombre,
            descripcion = descripcion,
            valor = valor,
            cantidadDisponible = cantidadDisponible,
            unidadMedida = unidadMedida,
            nivelMinimo = nivelMinimo,
            estadoStock = estado,
            imagenes = listImagenes
        )
    }

    companion object {
        /** Convierte un modelo de dominio [Producto] en una entidad de tabla Room [ProductoEntity]. */
        fun fromModel(p: Producto): ProductoEntity {
            return ProductoEntity(
                codigo = p.codigo,
                nombre = p.nombre,
                descripcion = p.descripcion,
                valor = p.valor,
                cantidadDisponible = p.cantidadDisponible,
                unidadMedida = p.unidadMedida,
                nivelMinimo = p.nivelMinimo,
                estadoStock = p.estadoStock.name,
                imagenesJson = p.imagenes.joinToString(";")
            )
        }
    }
}
