package com.blorex.appcappucare
import com.blorex.appcappucare.repository.MedicamentosRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MedicamentosRepositoryTest {
    @Test fun codigoVacioEsRechazadoAntesDeConsultarServidor() = runBlocking {
        val repo = MedicamentosRepository()
        try { repo.buscarPorCodigo("  "); fail("Debió rechazar un código vacío") }
        catch (e: IllegalArgumentException) { assertEquals("El código del medicamento no puede estar vacío", e.message) }
    }
}
