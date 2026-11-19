package com.blorex.appcappucare

import android.app.Application
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.FavoritosRepository
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.repository.SolicitudesRepository

/**
 * Clase Application personalizada para inicialización global de repositorios, base de datos SQLite/Room y temas visuales.
 */
class AppCappuCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa la base de datos local SQLite antes de crear cualquier pantalla o ViewModel
        ProductosRepository.inicializarBaseDeDatos(this)
        FavoritosRepository.inicializar(this)
        AuthRepository.inicializar(this)
        SolicitudesRepository.inicializar(this)
        ConfiguracionAppRepository.inicializar(this)
    }
}
