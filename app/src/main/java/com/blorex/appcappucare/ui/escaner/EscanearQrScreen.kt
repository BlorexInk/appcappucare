
package com.blorex.appcappucare.ui.escaner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.blorex.appcappucare.viewmodel.MedicamentosViewModel

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.runtime.LaunchedEffect

@Composable
fun EscanearQrScreen(
    modifier: Modifier = Modifier
) {

    val contexto = LocalContext.current

    // ViewModel encargado de consultar Node.js
    val medicamentosViewModel: MedicamentosViewModel = viewModel()

    var codigoQr by remember {
        mutableStateOf<String?>(null)
    }

    var mensaje by remember {
        mutableStateOf("Presiona el botón para escanear un código QR.")
    }

    // Actualizar el mensaje según el resultado de Node.js
    LaunchedEffect(
        medicamentosViewModel.cargando,
        medicamentosViewModel.medicamentoEncontrado,
        medicamentosViewModel.mensajeError
    ) {
        when {
            medicamentosViewModel.cargando -> {
                mensaje = "Consultando servidor..."
            }

            medicamentosViewModel.medicamentoEncontrado != null -> {
                mensaje = "Medicamento encontrado correctamente."
            }

            medicamentosViewModel.mensajeError != null -> {
                mensaje = medicamentosViewModel.mensajeError
                    ?: "Error al consultar medicamento"
            }
        }
    }

    // Configurar lector exclusivamente para códigos QR
    val opciones = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    // Crear lector de Google
    val escaner = remember(contexto) {
        GmsBarcodeScanning.getClient(contexto, opciones)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Escáner QR de medicamentos",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Escanea el código QR del medicamento.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                codigoQr = null
                medicamentosViewModel.limpiarBusqueda()

                mensaje = "Abriendo escáner..."

                escaner.startScan()

                    .addOnSuccessListener { resultado ->

                        val codigoDetectado = resultado.rawValue

                        if (!codigoDetectado.isNullOrBlank()) {

                            codigoQr = codigoDetectado

                            mensaje = "Código leído. Consultando servidor..."

                            // Buscar medicamento en Node.js
                            medicamentosViewModel.buscarMedicamento(
                                codigoDetectado
                            )

                        } else {

                            mensaje = "No se pudo obtener el código QR."
                        }
                    }

                    .addOnCanceledListener {

                        mensaje = "Escaneo cancelado."
                    }

                    .addOnFailureListener { error ->

                        mensaje = "Error al escanear: ${error.message}"
                    }

            },
            enabled = !medicamentosViewModel.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {

            Text("ESCANEAR CÓDIGO QR")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mostrar código leído
        codigoQr?.let { codigo ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Código detectado",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = codigo,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar indicador de consulta
        if (medicamentosViewModel.cargando) {

            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(12.dp))

            Text("Buscando medicamento en la base de datos...")
        }

        // Mostrar error de consulta
        medicamentosViewModel.mensajeError?.let { error ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Resultado de la búsqueda",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Mostrar medicamento encontrado en SQLite
        medicamentosViewModel.medicamentoEncontrado?.let { medicamento ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Medicamento encontrado",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Nombre: ${medicamento.nombre}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Código: ${medicamento.codigo}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Principio activo: ${medicamento.principioActivo}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Concentración: ${medicamento.concentracion}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Laboratorio: ${medicamento.laboratorio}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Stock disponible: ${medicamento.stock}")
                }
            }
        }
    }
}
