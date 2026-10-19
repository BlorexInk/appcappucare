package com.blorex.appcappucare.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Opciones disponibles para la configuración del tema visual de la aplicación.
 */
enum class OpcionTema {
    /** Ajusta el tema según la configuración global del sistema/dispositivo. */
    SISTEMA,

    /** Fuerza la apariencia clara (Light Theme). */
    CLARO,

    /** Fuerza la apariencia oscura (Dark Theme). */
    OSCURO
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Tema principal de AppCappuCare con soporte para selección de tema claro, oscuro o definido por el dispositivo.
 *
 * @param opcionTema Opción de tema seleccionada por el usuario ([OpcionTema.SISTEMA], [OpcionTema.CLARO], [OpcionTema.OSCURO]).
 * @param dynamicColor Si es `true`, aplica colores dinámicos en dispositivos Android 12+.
 * @param content Contenido composable envuelto por el tema.
 */
@Composable
fun AppcappucareTheme(
    opcionTema: OpcionTema = OpcionTema.SISTEMA,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (opcionTema) {
        OpcionTema.SISTEMA -> isSystemInDarkTheme()
        OpcionTema.CLARO -> false
        OpcionTema.OSCURO -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
