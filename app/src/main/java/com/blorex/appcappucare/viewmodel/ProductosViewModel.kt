package com.blorex.appcappucare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.repository.ProductosRepository

/**
 * ViewModel que administra el estado y operaciones CRUD de la pantalla de Gestión de Productos.
 */
class ProductosViewModel : ViewModel() {

    /** Filtro de búsqueda por código o nombre de producto. */
    var busqueda by mutableStateOf("")

    /** Control del diálogo de formulario (Agregar / Editar producto). */
    var mostrarDialogoFormulario by mutableStateOf(false)
        private set

    /** Producto seleccionado para edición (`null` si se está creando un nuevo producto). */
    var productoEnEdicion by mutableStateOf<Producto?>(null)
        private set

    /** Control del diálogo modal de confirmación de borrado. */
    var mostrarDialogoEliminar by mutableStateOf(false)
        private set

    /** Producto seleccionado para ser eliminado. */
    var productoAEliminar by mutableStateOf<Producto?>(null)
        private set

    /** Mensaje de error de validación en el formulario. */
    var mensajeError by mutableStateOf<String?>(null)

    /** Lista filtrada de productos en base al texto ingresado en [busqueda]. */
    val productos: List<Producto>
        get() {
            val lista = ProductosRepository.getProductosFicticios()
            if (busqueda.isBlank()) return lista
            return lista.filter {
                it.nombre.contains(busqueda, ignoreCase = true) ||
                it.codigo.contains(busqueda, ignoreCase = true)
            }
        }

    /** Abre el formulario para registrar un nuevo producto. */
    fun abrirFormularioNuevo() {
        productoEnEdicion = null
        mensajeError = null
        mostrarDialogoFormulario = true
    }

    /**
     * Abre el formulario precargado con los datos del producto seleccionado para edición.
     *
     * @param producto Producto a modificar.
     */
    fun abrirFormularioEditar(producto: Producto) {
        productoEnEdicion = producto
        mensajeError = null
        mostrarDialogoFormulario = true
    }

    /** Cierra el formulario modal de producto. */
    fun cerrarFormulario() {
        mostrarDialogoFormulario = false
        productoEnEdicion = null
        mensajeError = null
    }

    /**
     * Abre el cuadro de diálogo de confirmación para eliminar un producto.
     *
     * @param producto Producto a borrar.
     */
    fun solicitarEliminacion(producto: Producto) {
        productoAEliminar = producto
        mostrarDialogoEliminar = true
    }

    /** Cancela la acción de eliminación. */
    fun cancelarEliminacion() {
        mostrarDialogoEliminar = false
        productoAEliminar = null
    }

    /** Confirma y ejecuta la eliminación del producto seleccionado. */
    fun confirmarEliminacion() {
        productoAEliminar?.let {
            ProductosRepository.eliminarProducto(it.codigo)
        }
        mostrarDialogoEliminar = false
        productoAEliminar = null
    }

    /**
     * Valida y guarda un producto (creación o actualización) en el repositorio.
     *
     * @param codigo Código único.
     * @param nombre Nombre descriptivo.
     * @param cantidadDisponible Cantidad en stock.
     * @param unidadMedida Unidad de medida.
     * @param nivelMinimo Nivel mínimo de seguridad.
     * @return `true` si el guardado fue exitoso; `false` si hubo errores de validación o código duplicado.
     */
    fun guardarProducto(
        codigo: String,
        nombre: String,
        cantidadDisponible: Int,
        unidadMedida: String,
        nivelMinimo: Int
    ): Boolean {
        if (codigo.isBlank() || nombre.isBlank() || unidadMedida.isBlank()) {
            mensajeError = "Todos los campos son obligatorios"
            return false
        }

        val esEdicion = productoEnEdicion != null
        val producto = Producto(
            codigo = codigo.trim().uppercase(),
            nombre = nombre.trim(),
            cantidadDisponible = cantidadDisponible,
            unidadMedida = unidadMedida.trim(),
            nivelMinimo = nivelMinimo,
            estadoStock = ProductosRepository.calcularEstadoStock(cantidadDisponible, nivelMinimo)
        )

        val exito = if (esEdicion) {
            ProductosRepository.actualizarProducto(producto)
        } else {
            ProductosRepository.agregarProducto(producto)
        }

        if (!exito) {
            mensajeError = if (esEdicion) "No se pudo actualizar el producto" else "Ya existe un producto con el código $codigo"
            return false
        }

        cerrarFormulario()
        return true
    }
}
