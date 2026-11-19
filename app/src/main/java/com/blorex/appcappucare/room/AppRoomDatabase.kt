package com.blorex.appcappucare.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Producto
import com.blorex.appcappucare.model.SolicitudModificacion
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.room.dao.*
import com.blorex.appcappucare.room.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal de Android Jetpack Room para AppCappuCare.
 *
 * Mantiene la persistencia local de productos, favoritos, usuarios, solicitudes de TI y configuración visual.
 */
@Database(
    entities = [
        ProductoEntity::class,
        FavoritoEntity::class,
        UsuarioEntity::class,
        SolicitudEntity::class,
        ConfiguracionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class   AppRoomDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        /** Retorna la instancia Singleton de la base de datos de Room. */
        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "appcappucare_room.db"
                )
                    .addCallback(AppDatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** Callback de Room para precargar datos iniciales al crear la base de datos por primera vez. */
        private class AppDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        precargarDatosIniciales(database)
                    }
                }
            }
        }

        /** Precarga el inventario de insumos, usuarios por defecto y solicitudes iniciales. */
        private suspend fun precargarDatosIniciales(database: AppRoomDatabase) {
            val productosIniciales = listOf(
                Producto("JR-10ML", "Jeringa 10ml", "Jeringa desechable estéril de 10ml para inyecciones y extracción de fluidos.", 1200.0, 45, "un", 20, EstadoStock.DISPONIBLE, listOf("jeringa_1.jpg", "jeringa_2.jpg")),
                Producto("MK95-01", "Mascarilla KN95", "Mascarilla de protección respiratoria KN95 de 5 capas de alta filtración.", 800.0, 5, "un", 20, EstadoStock.STOCK_BAJO, listOf("mascarilla_1.jpg")),
                Producto("GN-NL-M", "Guantes Nitrilo M", "Caja con 100 guantes de nitrilo sin polvo tamaño Mediano para examen médico.", 8500.0, 0, "caja", 10, EstadoStock.AGOTADO, listOf("guantes_1.jpg", "guantes_2.jpg")),
                Producto("GS-EST-10", "Gasas Estériles", "Paquete con 10 sobres de gasa estéril de tejido 100% algodón 10x10cm.", 1500.0, 100, "paq", 30, EstadoStock.DISPONIBLE, listOf("gasa_1.jpg")),
                Producto("ALC-70-1L", "Alcohol Etílico 70% 1L", "Frasco de alcohol antiséptico de 70 grados de 1 Litro para desinfección.", 3200.0, 25, "frasco", 10, EstadoStock.DISPONIBLE, listOf("alcohol_1.jpg")),
                Producto("OXI-PUL-01", "Oxímetro de Pulso", "Oxímetro de pulso para dedo digital de alta precisión con pantalla OLED.", 24900.0, 12, "un", 5, EstadoStock.DISPONIBLE, listOf("oximetro_1.jpg", "oximetro_2.jpg")),
                Producto("TERM-DIG-01", "Termómetro Digital", "Termómetro digital corporal de lectura rápida con alarma de fiebre.", 4500.0, 18, "un", 8, EstadoStock.DISPONIBLE, listOf("termometro_1.jpg")),
                Producto("VEN-ELA-10", "Venda Elástica 10cm", "Venda elástica de compresión médica de 10cm x 5m con sujetadores.", 2100.0, 30, "un", 15, EstadoStock.DISPONIBLE, listOf("venda_1.jpg")),
                Producto("SOL-SAL-500", "Solución Salina 0.9% 500ml", "Solución fisiológica de cloruro de sodio 0.9% para irrigación o suero.", 2800.0, 50, "un", 20, EstadoStock.DISPONIBLE, listOf("suero_1.jpg")),
                Producto("ALG-HYG-500", "Algodón Hidrófilo 500g", "Paquete de algodón hidrófilo de 500 gramos 100% puro para curaciones.", 3900.0, 15, "paq", 10, EstadoStock.DISPONIBLE, listOf("algodon_1.jpg")),
                Producto("CAT-IV-20G", "Catéter Intravenoso 20G", "Catéter periférico intravenoso 20G con teflón estéril de seguridad.", 1900.0, 80, "un", 25, EstadoStock.DISPONIBLE, listOf("cateter_1.jpg")),
                Producto("TENS-DIG-01", "Tensiómetro Digital", "Monitor de presión arterial automático de brazo con memoria de lecturas.", 38900.0, 8, "un", 4, EstadoStock.DISPONIBLE, listOf("tensiometro_1.jpg", "tensiometro_2.jpg")),
                Producto("JAB-ANT-1L", "Jabón Antiséptico 1L", "Jabón líquido quirúrgico y antiséptico de clorhexidina 1L.", 6800.0, 22, "frasco", 10, EstadoStock.DISPONIBLE, listOf("jabon_1.jpg")),
                Producto("CINT-MIC-25", "Cinta Micropore 2.5cm", "Cinta médica quirúrgica de papel hipoalergénico 2.5cm x 10m.", 1600.0, 40, "un", 15, EstadoStock.DISPONIBLE, listOf("cinta_1.jpg")),
                Producto("ESPAD-10", "Esparadrapo de Tela", "Rollo de esparadrapo de tela resistente de fijación 5cm x 5m.", 2300.0, 35, "un", 10, EstadoStock.DISPONIBLE, listOf("esparadrapo_1.jpg")),
                Producto("JERING-03ML", "Jeringa 3ml con Aguja", "Jeringa desechable de 3ml con aguja hipodérmica 21G incorporada.", 950.0, 120, "un", 40, EstadoStock.DISPONIBLE, listOf("jeringa3ml_1.jpg")),
                Producto("GUA-LAT-L", "Guantes de Látex L", "Caja con 100 guantes de examen de látex natural estandarizado tamaño L.", 7900.0, 3, "caja", 10, EstadoStock.STOCK_BAJO, listOf("guantes_latex_1.jpg")),
                Producto("GOR-DES-100", "Gorros Desechables", "Bolsa con 100 gorros oruga desechables de polipropileno para clínica.", 4200.0, 200, "un", 50, EstadoStock.DISPONIBLE, listOf("gorros_1.jpg")),
                Producto("ZAP-DES-100", "Cubrezapatos Desechables", "Bolsa con 100 cubrezapatos antideslizantes desechables azulin.", 4800.0, 150, "un", 40, EstadoStock.DISPONIBLE, listOf("cubrezapatos_1.jpg")),
                Producto("SUT-NIR-30", "Hilo de Sutura Nylon 3-0", "Sobre con hilo de sutura monofilamento de nylon 3-0 con aguja corta.", 3500.0, 0, "un", 12, EstadoStock.AGOTADO, listOf("sutura_1.jpg"))
            )

            database.productoDao().guardarTodos(productosIniciales.map { ProductoEntity.fromDomainModel(it) })

            // Favoritos iniciales
            val favs = listOf(FavoritoEntity("MK95-01"), FavoritoEntity("TERM-DIG-01"))
            database.favoritoDao().agregarTodos(favs)

            // Usuarios iniciales
            val usuariosIniciales = listOf(
                Usuario("blorex", "1234", "Carlos Blorex", "TI", "Sistemas / TI", "blorexink@gmail.com", "+56 9 1234 5678", "Av. Principal 100", "Bodega Central XXXX"),
                Usuario("admin", "admin123", "Ana Martínez", "Administrador", "Administración General", "blorexink@gmail.com", "+56 9 8765 4321", "Calle Centro 250", "Bodega Central XXXX"),
                Usuario("gerente", "gerente123", "Roberto Silva", "Gerente de Tienda", "Gerencia de Tienda", "blorexink@gmail.com", "+56 9 5555 6666", "Av. Los Leones 50", "Bodega Central XXXX"),
                Usuario("vendedor", "pass123", "Luis Gómez", "Vendedor", "Ventas y Mostrador", "blorexink@gmail.com", "+56 9 4444 3333", "Pasaje Sol 12", "Bodega Central XXXX")
            )
            database.usuarioDao().guardarTodos(usuariosIniciales.map { UsuarioEntity.fromDomainModel(it) })

            // Solicitud inicial
            val sol1 = SolicitudModificacion(
                usernameEmpleado = "vendedor",
                nombreEmpleado = "Luis Gómez",
                rolEmpleado = "Vendedor",
                asunto = "Cambio de Teléfono de Contacto",
                detalle = "Solicito actualizar mi teléfono de contacto al +56 9 9988 7766 por cambio de número corporativo."
            )
            database.solicitudDao().guardarOActualizarSolicitud(SolicitudEntity.fromDomainModel(sol1))
        }
    }
}
