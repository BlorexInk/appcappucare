package com.blorex.appcappucare.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blorex.appcappucare.model.Producto
import java.text.NumberFormat
import java.util.*

/**
 * Utilidad encargada de generar y transmitir la ficha descriptiva de insumos médicos
 * a aplicaciones de mensajería (WhatsApp, Telegram, Correo, Mensajes) mediante Intents nativos de Android.
 */
object CompartirUtils {

    /**
     * Construye la ficha informativa del producto y dispara el selector nativo de compartir de Android.
     *
     * @param context Contexto de la actividad/aplicación.
     * @param producto Insumo médico a compartir.
     */
    fun compartirProducto(context: Context, producto: Producto) {
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }

        val primeraImagenUri = producto.imagenes.firstOrNull() ?: ""

        val textoCompartir = """
            🏥 AppCappuCare - Insumo Médico
            
            📌 Producto: ${producto.nombre}
            🔢 Código Identificador: ${producto.codigo}
            💲 Precio: ${formatoMoneda.format(producto.valor)}
            📦 Stock Disponible: ${producto.cantidadDisponible} ${producto.unidadMedida}
            📝 Descripción: ${if (producto.descripcion.isNotBlank()) producto.descripcion else "Insumo médico hospitalario de alta calidad para clínica y salud."}
            
            📲 Descarga la AppCappuCare para realizar tus pedidos y consultar el catálogo completo.
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Insumo Médico AppCappuCare: ${producto.nombre}")
            putExtra(Intent.EXTRA_TEXT, textoCompartir)

            if (primeraImagenUri.startsWith("content://") || primeraImagenUri.startsWith("file://")) {
                try {
                    val uri = Uri.parse(primeraImagenUri)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    // Fallback a texto
                }
            }
        }

        val chooser = Intent.createChooser(sendIntent, "Compartir insumo '${producto.nombre}' vía...")
        context.startActivity(chooser)
    }
}
