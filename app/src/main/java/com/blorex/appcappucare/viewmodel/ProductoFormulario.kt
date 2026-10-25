package com.blorex.appcappucare.viewmodel

/** Campos del formulario de producto que pueden mostrar un error de validación. */
enum class CampoProducto {
    NOMBRE,
    CODIGO,
    PRECIO,
    STOCK,
    UNIDAD,
    NIVEL_MINIMO
}

/**
 * Datos crudos que la interfaz entrega al ViewModel para validar y guardar.
 * Los números llegan como texto para que la validación viva en la lógica y no en la pantalla.
 */
data class ProductoFormulario(
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val stock: String,
    val unidad: String,
    val nivelMinimo: String,
    val imagenes: List<String> = emptyList(),
    val fotoUri: String? = null
)
