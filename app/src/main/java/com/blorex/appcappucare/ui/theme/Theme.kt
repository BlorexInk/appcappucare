package com.blorex.appcappucare.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.GamaColorApp

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

/**
 * Obtiene el esquema de colores en base a la gama seleccionada por TI ([GamaColorApp]) y la preferencia de tema (Claro/Oscuro).
 */
fun obtenerEsquemaColoresGama(gama: GamaColorApp, esOscuro: Boolean): ColorScheme {
    val (primaryColor, secondaryColor) = when (gama) {
        GamaColorApp.VERDE_SALUD -> Pair(VerdeSaludPrimary, VerdeSaludSecondary)
        GamaColorApp.ROJO_PALIDO -> Pair(RojoPalidoPrimary, RojoPalidoSecondary)
        GamaColorApp.AZUL_HOSPITALARIO -> Pair(AzulClaroPrimary, AzulClaroSecondary)
        GamaColorApp.AZUL_CORPORATIVO -> Pair(AzulCorporativoPrimary, AzulCorporativoSecondary)
        GamaColorApp.PURPURA_SALUD -> Pair(PurpuraSaludPrimary, PurpuraSaludSecondary)
    }

    return if (esOscuro) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = secondaryColor,
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = secondaryColor
        )
    }
}

/**
 * Tema principal de AppCappuCare con soporte para selección de gama de colores por TI y temas claro/oscuro.
 *
 * @param opcionTema Opción de tema seleccionada por el usuario ([OpcionTema.SISTEMA], [OpcionTema.CLARO], [OpcionTema.OSCURO]).
 * @param dynamicColor Si es `true`, aplica colores dinámicos cuando corresponda.
 * @param content Contenido composable envuelto por el tema.
 */
@Composable
fun AppcappucareTheme(
    opcionTema: OpcionTema = OpcionTema.SISTEMA,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (opcionTema) {
        OpcionTema.SISTEMA -> isSystemInDarkTheme()
        OpcionTema.CLARO -> false
        OpcionTema.OSCURO -> true
    }

    val gamaSeleccionada = ConfiguracionAppRepository.gamaColorSeleccionada

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> obtenerEsquemaColoresGama(gamaSeleccionada, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
