package com.blorex.appcappucare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blorex.appcappucare.model.EstadoStock
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.repository.AuthRepository
import com.blorex.appcappucare.repository.ConfiguracionAppRepository
import com.blorex.appcappucare.repository.ProductosRepository
import com.blorex.appcappucare.repository.SolicitudesRepository
import com.blorex.appcappucare.repository.sync.ModoConexion
import com.blorex.appcappucare.repository.sync.SyncBackendRepository
import com.blorex.appcappucare.ui.admin.ModificacionAppDialog
import com.blorex.appcappucare.ui.auth.LoginScreen
import com.blorex.appcappucare.ui.carrito.CarritoScreen
import com.blorex.appcappucare.ui.catalog.CatalogoShowcaseScreen
import com.blorex.appcappucare.ui.components.BurbujaAlertaFlotante
import com.blorex.appcappucare.ui.components.CargarImagenProducto
import com.blorex.appcappucare.ui.dashboard.DashboardNotificacionesScreen
import com.blorex.appcappucare.ui.favoritos.FavoritosScreen
import com.blorex.appcappucare.ui.productos.ProductosScreen
import com.blorex.appcappucare.ui.theme.AppcappucareTheme
import com.blorex.appcappucare.ui.theme.OpcionTema
import com.blorex.appcappucare.ui.usuarios.BuzonSolicitudesDialog
import com.blorex.appcappucare.ui.usuarios.GestionUsuariosDialog
import com.blorex.appcappucare.ui.usuarios.PerfilUsuarioDialog
import com.blorex.appcappucare.ui.usuarios.UsuariosActivosDialog
import com.blorex.appcappucare.ui.ventas.HistorialVentasDialog
import com.blorex.appcappucare.viewmodel.AuthViewModel
import com.blorex.appcappucare.viewmodel.VentaViewModel
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación AppCappuCare con Control de Acceso por Roles (RBAC).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProductosRepository.inicializarBaseDeDatos(this)
        AuthRepository.inicializar(this)
        SolicitudesRepository.inicializar(this)
        ConfiguracionAppRepository.inicializar(this)
        setContent {
            AppNavigationRoot()
        }
    }
}

/**
 * Componente raíz de navegación y envoltorio del tema [AppcappucareTheme].
 */
@Composable
fun AppNavigationRoot(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ProductosRepository.inicializarBaseDeDatos(context)
        AuthRepository.inicializar(context)
        SolicitudesRepository.inicializar(context)
        ConfiguracionAppRepository.inicializar(context)
    }

    AppcappucareTheme(opcionTema = authViewModel.opcionTema) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val usuario = authViewModel.usuarioLogueado

            if (usuario == null) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { /* Autenticado */ }
                )
            } else {
                MainAppLayout(
                    usuario = usuario,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

/**
 * Layout principal con Control de Acceso basado en Roles (RBAC).
 *
 * Restringe funciones exclusivas para TI (`blorex`), Administradores y Vendedores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    ventaViewModel: VentaViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val esTI = usuario.username.equals("blorex", ignoreCase = true) || usuario.rol.equals("TI", ignoreCase = true)
    val esVendedor = usuario.rol.equals("Vendedor", ignoreCase = true)
    val puedeVerReportesVentas = esTI || usuario.rol.equals("Administrador", ignoreCase = true) || usuario.rol.equals("Gerente de Tienda", ignoreCase = true)

    // Pantalla inicial por defecto: Catálogo (0)
    var selectedTab by remember { mutableStateOf(0) }

    var mostrarPerfilDialog by remember { mutableStateOf(false) }
    var mostrarAjustesDialog by remember { mutableStateOf(false) }
    var mostrarAlertasDesdeDrawer by remember { mutableStateOf(false) }
    var mostrarUsuariosActivos by remember { mutableStateOf(false) }
    var mostrarGestionUsuarios by remember { mutableStateOf(false) }
    var mostrarBuzonSolicitudes by remember { mutableStateOf(false) }
    var mostrarHistorialVentas by remember { mutableStateOf(false) }
    var mostrarModificacionApp by remember { mutableStateOf(false) }
    var mostrarBurbujaFlotante by remember { mutableStateOf(true) }

    val totalItemsEnCarrito = ventaViewModel.carrito.value.sumOf { it.cantidadVendida }
    val todosLosProductos = ProductosRepository.getProductosFicticios()
    val totalAlertas = todosLosProductos.count { it.estadoStock == EstadoStock.AGOTADO || it.estadoStock == EstadoStock.STOCK_BAJO }
    val conteoSolicitudesPendientes = SolicitudesRepository.conteoPendientes()
    val logoAppUri = ConfiguracionAppRepository.logoAppUri

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (logoAppUri.isNotBlank()) {
                                CargarImagenProducto(
                                    uriString = logoAppUri,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = usuario.username.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${usuario.rol} (${usuario.categoriaCargo})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawer Option: Alertas de Stock
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        label = { Text("Alertas de Stock ($totalAlertas)") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarAlertasDesdeDrawer = true
                        }
                    )

                    // Drawer Option: Historial y Reportes de Ventas
                    if (puedeVerReportesVentas) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.PointOfSale, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("Historial & Reportes Ventas") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                mostrarHistorialVentas = true
                            }
                        )
                    }

                    // Drawer Option: Usuarios Activos & Horarios
                    if (puedeVerReportesVentas) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("Usuarios Activos") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                mostrarUsuariosActivos = true
                            }
                        )
                    }

                    // Drawer Option EXCLUSIVA PARA TI (`blorex`): Gestión de Usuarios
                    if (esTI) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                            label = { Text("Gestión de Usuarios (Solo TI)") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                mostrarGestionUsuarios = true
                            }
                        )

                        // Drawer Option EXCLUSIVA PARA TI: Buzón de Solicitudes de Trabajadores
                        NavigationDrawerItem(
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (conteoSolicitudesPendientes > 0) {
                                            Badge { Text("$conteoSolicitudesPendientes") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                }
                            },
                            label = { Text("Solicitudes de Trabajadores") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                mostrarBuzonSolicitudes = true
                            }
                        )
                    }

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Mi Perfil") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarPerfilDialog = true
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Ajustes de la App") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarAjustesDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        label = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            authViewModel.cerrarSesion()
                        }
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Catálogo de Productos"
                                    1 -> "Productos Guardados / Favoritos"
                                    2 -> "Carrito de Compras"
                                    3 -> if (esVendedor) "AppCappuCare" else "Inventario de Productos"
                                    else -> "AppCappuCare"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú de Opciones (3 barras)"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Storefront, contentDescription = "Catálogo") },
                            label = { Text("Catálogo") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                            label = { Text("Favoritos") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalItemsEnCarrito > 0) {
                                            Badge { Text("$totalItemsEnCarrito") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                                }
                            },
                            label = { Text("Carrito") }
                        )

                        // Pestaña Inventario visible SOLO para TI, Admin y Gerente (Oculta para Vendedor)
                        if (!esVendedor) {
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                                label = { Text("Inventario") }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> CatalogoShowcaseScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                    1 -> FavoritosScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                    2 -> CarritoScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel, usuario = usuario)
                    3 -> if (!esVendedor) ProductosScreen(modifier = Modifier.padding(innerPadding)) else CatalogoShowcaseScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                }
            }

            // Draggable Red Floating Alert Bubble Overlay
            if (mostrarBurbujaFlotante && totalAlertas > 0) {
                BurbujaAlertaFlotante(
                    totalAlertas = totalAlertas,
                    onAbrirAlertas = { mostrarAlertasDesdeDrawer = true },
                    onDescartarBurbuja = { mostrarBurbujaFlotante = false }
                )
            }
        }
    }

    // Modal para Alertas de Stock
    if (mostrarAlertasDesdeDrawer) {
        Dialog(
            onDismissRequest = { mostrarAlertasDesdeDrawer = false }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertas de Stock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { mostrarAlertasDesdeDrawer = false }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DashboardNotificacionesScreen()
                }
            }
        }
    }

    // Modal EXCLUSIVO TI (`blorex`): Modificación de App (Logo, Portadas y Gamas de Colores)
    if (mostrarModificacionApp && esTI) {
        ModificacionAppDialog(
            onDismiss = { mostrarModificacionApp = false }
        )
    }

    // Modal para Historial y Reportes de Ventas
    if (mostrarHistorialVentas && puedeVerReportesVentas) {
        HistorialVentasDialog(
            usuarioLogueado = usuario,
            onDismiss = { mostrarHistorialVentas = false }
        )
    }

    // Modal para Usuarios Activos & Horarios
    if (mostrarUsuariosActivos) {
        UsuariosActivosDialog(
            onDismiss = { mostrarUsuariosActivos = false }
        )
    }

    // Modal EXCLUSIVO TI (blorex): Gestión de Usuarios
    if (mostrarGestionUsuarios && esTI) {
        GestionUsuariosDialog(
            onDismiss = { mostrarGestionUsuarios = false }
        )
    }

    // Modal EXCLUSIVO TI (blorex): Buzón de Solicitudes de Trabajadores
    if (mostrarBuzonSolicitudes && esTI) {
        BuzonSolicitudesDialog(
            onDismiss = { mostrarBuzonSolicitudes = false }
        )
    }

    // Modal para Perfil del Usuario
    if (mostrarPerfilDialog) {
        PerfilUsuarioDialog(
            usuario = usuario,
            usuarioLogueado = usuario,
            onDismiss = { mostrarPerfilDialog = false }
        )
    }

    // Modal dialog for Ajustes with Theme, Sync Mode and TI App Customization
    if (mostrarAjustesDialog) {
        AlertDialog(
            onDismissRequest = { mostrarAjustesDialog = false },
            title = { Text("Ajustes de la Aplicación") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificaciones de stock bajo")
                        Switch(
                            checked = authViewModel.notificacionesActivas,
                            onCheckedChange = { authViewModel.notificacionesActivas = it }
                        )
                    }

                    HorizontalDivider()

                    // Opción para la personalización de TI en Ajustes
                    if (esTI) {
                        Button(
                            onClick = {
                                mostrarAjustesDialog = false
                                mostrarModificacionApp = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificación de App (Logo, Portadas y Colores)")
                        }

                        HorizontalDivider()
                    }

                    // Modo de Conexión: Offline Room vs Online Backend
                    Text(
                        text = "Modo de Base de Datos / Conexión:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val opcionesModo = listOf(
                        ModoConexion.OFFLINE_ROOM to "Modo Offline (Room / SQLite Local)",
                        ModoConexion.ONLINE_BACKEND to "Modo Online (Sincronización Backend)"
                    )

                    opcionesModo.forEach { (modo, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (SyncBackendRepository.modoConexion == modo),
                                    onClick = { SyncBackendRepository.modoConexion = modo },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (SyncBackendRepository.modoConexion == modo),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (SyncBackendRepository.modoConexion == ModoConexion.ONLINE_BACKEND) {
                        OutlinedTextField(
                            value = SyncBackendRepository.servidorUrlInput,
                            onValueChange = { SyncBackendRepository.servidorUrlInput = it },
                            label = { Text("URL Servidor Backend") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { SyncBackendRepository.sincronizarConServidorBackend(context, scope) },
                            enabled = !SyncBackendRepository.estaSincronizando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (SyncBackendRepository.estaSincronizando) "Transmitiendo datos..." else "Sincronizar con Servidor Backend")
                        }

                        SyncBackendRepository.ultimoMensajeSincronizacion?.let { msg ->
                            Text(text = msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider()

                    // Modo de Tema (Independiente de los colores)
                    Text(
                        text = "Apariencia y Modo de Pantalla (Independiente):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val opcionesTema = listOf(
                        OpcionTema.SISTEMA to "Definida por el dispositivo",
                        OpcionTema.CLARO to "Pantalla Clara (Light Theme)",
                        OpcionTema.OSCURO to "Pantalla Oscura (Dark Theme)"
                    )

                    opcionesTema.forEach { (opcion, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (authViewModel.opcionTema == opcion),
                                    onClick = { authViewModel.opcionTema = opcion },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (authViewModel.opcionTema == opcion),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    HorizontalDivider()

                    Text("Versión de AppCappuCare: 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Text("Base de datos: Room / SQLite Local", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { mostrarAjustesDialog = false }) {
                    Text("Guardar")
                }
            }
        )
    }
}
