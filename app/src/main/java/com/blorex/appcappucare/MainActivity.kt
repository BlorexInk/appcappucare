package com.blorex.appcappucare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blorex.appcappucare.model.Usuario
import com.blorex.appcappucare.ui.auth.LoginScreen
import com.blorex.appcappucare.ui.carrito.CarritoScreen
import com.blorex.appcappucare.ui.catalog.CatalogoShowcaseScreen
import com.blorex.appcappucare.ui.dashboard.DashboardNotificacionesScreen
import com.blorex.appcappucare.ui.favoritos.FavoritosScreen
import com.blorex.appcappucare.ui.productos.ProductosScreen
import com.blorex.appcappucare.ui.theme.AppcappucareTheme
import com.blorex.appcappucare.ui.theme.OpcionTema
import com.blorex.appcappucare.viewmodel.AuthViewModel
import com.blorex.appcappucare.viewmodel.VentaViewModel
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación AppCappuCare.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
 * Layout principal cuando el usuario ha iniciado sesión.
 *
 * Contiene la barra superior [TopAppBar] con botón de 3 barras, el menú lateral [ModalNavigationDrawer]
 * con accesos a Perfil, Ajustes y Cerrar Sesión, y la barra inferior de pestañas [NavigationBar].
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
    var selectedTab by remember { mutableStateOf(0) }

    var mostrarPerfilDialog by remember { mutableStateOf(false) }
    var mostrarAjustesDialog by remember { mutableStateOf(false) }

    val totalItemsEnCarrito = ventaViewModel.carrito.value.sumOf { it.cantidadVendida }

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
                            Text(
                                text = usuario.username.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${usuario.rol} (@${usuario.username})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil de Usuario") },
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Notificaciones & Stock"
                                1 -> "Catálogo de Productos"
                                2 -> "Productos Guardados / Favoritos"
                                3 -> "Carrito de Compras"
                                4 -> "Inventario de Productos"
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
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas") },
                        label = { Text("Alertas") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Catálogo") },
                        label = { Text("Catálogo") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                        label = { Text("Favoritos") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
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
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                        label = { Text("Inventario") }
                    )
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> DashboardNotificacionesScreen(modifier = Modifier.padding(innerPadding))
                1 -> CatalogoShowcaseScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                2 -> FavoritosScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                3 -> CarritoScreen(modifier = Modifier.padding(innerPadding), ventaViewModel = ventaViewModel)
                4 -> ProductosScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    // Modal dialog for Perfil
    if (mostrarPerfilDialog) {
        AlertDialog(
            onDismissRequest = { mostrarPerfilDialog = false },
            title = { Text("Perfil de Usuario") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Nombre: ${usuario.nombreCompleto}", fontWeight = FontWeight.SemiBold)
                    Text("• Usuario: ${usuario.username}")
                    Text("• Rol: ${usuario.rol}")
                    Text("• Email: ${usuario.email}")
                }
            },
            confirmButton = {
                Button(onClick = { mostrarPerfilDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Modal dialog for Ajustes with Theme selection (Dark / Light / System)
    if (mostrarAjustesDialog) {
        AlertDialog(
            onDismissRequest = { mostrarAjustesDialog = false },
            title = { Text("Ajustes de la Aplicación") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

                    Text(
                        text = "Apariencia y Tema de Pantalla:",
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
                                .padding(vertical = 4.dp),
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
                    Text("Servidor de datos: Local (Sincronizado)", style = MaterialTheme.typography.bodySmall)
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
