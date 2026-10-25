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

    /** Mensaje de error general del formulario (por ejemplo, un fallo al guardar). */
    var mensajeError by mutableStateOf<String?>(null)

    /** Errores de validación por campo del formulario de producto. */
    var erroresFormulario by mutableStateOf<Map<CampoProducto, String>>(emptyMap())
        private set

    private val regexCodigo = Regex("^[A-Z0-9-]{2,20}$")

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
        erroresFormulario = emptyMap()
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
        erroresFormulario = emptyMap()
        mostrarDialogoFormulario = true
    }

    /** Cierra el formulario modal de producto. */
    fun cerrarFormulario() {
        mostrarDialogoFormulario = false
        productoEnEdicion = null
        mensajeError = null
        erroresFormulario = emptyMap()
    }

    /** Quita el error de un campo cuando el usuario vuelve a editarlo. */
    fun limpiarError(campo: CampoProducto) {
        if (erroresFormulario.containsKey(campo)) {
            erroresFormulario = erroresFormulario - campo
        }
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
     * Valida todos los campos del formulario y devuelve un mapa campo -> mensaje de error.
     * Un mapa vacío significa que el formulario es válido.
     */
    private fun validarFormulario(f: ProductoFormulario, esEdicion: Boolean): Map<CampoProducto, String> {
        val errores = mutableMapOf<CampoProducto, String>()

        val nombre = f.nombre.trim()
        when {
            nombre.isEmpty() -> errores[CampoProducto.NOMBRE] = "Ingresa el nombre del producto"
            nombre.length < 3 -> errores[CampoProducto.NOMBRE] = "Debe tener al menos 3 caracteres"
            nombre.length > 60 -> errores[CampoProducto.NOMBRE] = "Máximo 60 caracteres"
        }

        val codigo = f.codigo.trim().uppercase()
        when {
            codigo.isEmpty() -> errores[CampoProducto.CODIGO] = "El código es obligatorio"
            !regexCodigo.matches(codigo) ->
                errores[CampoProducto.CODIGO] = "Usa de 2 a 20 letras, números o guiones"
            !esEdicion && ProductosRepository.getProductosFicticios()
                .any { it.codigo.equals(codigo, ignoreCase = true) } ->
                errores[CampoProducto.CODIGO] = "Ya existe un producto con este código"
        }

        val precio = f.precio.toIntOrNull()
        when {
            f.precio.isBlank() -> errores[CampoProducto.PRECIO] = "Ingresa el precio"
            precio == null -> errores[CampoProducto.PRECIO] = "El valor es demasiado grande"
            precio <= 0 -> errores[CampoProducto.PRECIO] = "El precio debe ser mayor a 0"
        }

        when {
            f.stock.isBlank() -> errores[CampoProducto.STOCK] = "Ingresa el stock"
            f.stock.toIntOrNull() == null -> errores[CampoProducto.STOCK] = "El valor es demasiado grande"
        }

        val unidad = f.unidad.trim()
        when {
            unidad.isEmpty() -> errores[CampoProducto.UNIDAD] = "Ingresa la unidad (ej: un)"
            unidad.length > 10 -> errores[CampoProducto.UNIDAD] = "Máximo 10 caracteres"
        }

        when {
            f.nivelMinimo.isBlank() -> errores[CampoProducto.NIVEL_MINIMO] = "Ingresa el nivel mínimo"
            f.nivelMinimo.toIntOrNull() == null ->
                errores[CampoProducto.NIVEL_MINIMO] = "El valor es demasiado grande"
        }

        return errores
    }

    /**
     * Valida y guarda un producto (creación o actualización).
     *
     * @param formulario Datos ingresados en el formulario.
     * @return `true` si se guardó; `false` si hubo errores de validación o falló el guardado.
     */
    fun guardarProducto(formulario: ProductoFormulario): Boolean {
        val esEdicion = productoEnEdicion != null
        val errores = validarFormulario(formulario, esEdicion)
        erroresFormulario = errores
        if (errores.isNotEmpty()) {
            mensajeError = null
            return false
        }

        val cantidad = formulario.stock.toInt()
        val minimo = formulario.nivelMinimo.toInt()
        val codigo = productoEnEdicion?.codigo ?: formulario.codigo.trim().uppercase()

        val producto = Producto(
            codigo = codigo,
            nombre = formulario.nombre.trim(),
            descripcion = formulario.descripcion.trim(),
            valor = formulario.precio.toDouble(),
            cantidadDisponible = cantidad,
            unidadMedida = formulario.unidad.trim(),
            nivelMinimo = minimo,
            estadoStock = ProductosRepository.calcularEstadoStock(cantidad, minimo),
            imagenes = formulario.imagenes,
            fotoUri = formulario.fotoUri
        )

        val exito = if (esEdicion) {
            ProductosRepository.actualizarProducto(producto)
        } else {
            ProductosRepository.agregarProducto(producto)
        }

        if (!exito) {
            mensajeError = if (esEdicion) {
                "No se pudo actualizar el producto"
            } else {
                "No se pudo guardar el producto"
            }
            return false
        }

        cerrarFormulario()
        return true
    }
}
