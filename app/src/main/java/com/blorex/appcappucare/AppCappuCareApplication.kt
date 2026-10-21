package com.blorex.appcappucare

import android.app.Application
import com.blorex.appcappucare.repository.FavoritosRepository
import com.blorex.appcappucare.repository.ProductosRepository

/**
 * Clase Application personalizada para inicialización global de repositorios y base de datos SQLite/Room.
 */
class AppCappuCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa la base de datos local SQLite antes de crear cualquier pantalla o ViewModel
        ProductosRepository.inicializarBaseDeDatos(this)
        FavoritosRepository.inicializar(this)
    }
}
