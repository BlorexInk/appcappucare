package com.blorex.appcappucare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Componente de Burbuja Flotante de Alertas Arrastrable.
 *
 * Muestra un indicador circular rojo en pantalla con el número de alertas de stock.
 * Se puede arrastrar libremente mediante gestos ([detectDragGestures]) y descarte mediante
 * una Cruz (X) que se despliega en la parte inferior central cuando el usuario realiza el arrastre.
 *
 * @param totalAlertas Cantidad total de productos con alertas (Agotados + Stock Bajo).
 * @param onAbrirAlertas Callback al tocar la burbuja para ingresar al Centro de Notificaciones.
 * @param onDescartarBurbuja Callback al soltar la burbuja sobre la Cruz (X) inferior.
 */
@Composable
fun BurbujaAlertaFlotante(
    totalAlertas: Int,
    onAbrirAlertas: () -> Unit,
    onDescartarBurbuja: () -> Unit
) {
    if (totalAlertas <= 0) return

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Drop target: Bottom-Center Cross "X" zone displayed while dragging
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Surface(
                color = Color(0xFFC62828),
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier.size(70.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cruz de eliminación",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Draggable Floating Bubble Button
        Surface(
            color = Color(0xFFD32F2F),
            shape = CircleShape,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 16.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(60.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            // Si se arrastra suficientemente hacia abajo (zona de la cruz inferior)
                            if (offsetY > 400f) {
                                onDescartarBurbuja()
                            }
                        },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
                .clickable { onAbrirAlertas() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alertas de Stock",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                // Red Badge Count
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$totalAlertas",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
