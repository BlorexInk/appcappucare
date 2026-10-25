# AppCappuCare

Prototipo funcional de aplicación móvil Android para el **control inteligente y oportuno de existencias** de productos e insumos médicos de **Cappucare**.

---

## Integrantes

|Carlos Andres Guerra Pinilla
| Dastin Jesus Rojas Olea
| Mosiah Kolob Patricio Estrada Meneses 

---

## Descripción del proyecto

Cappucare comercializa productos e insumos médicos y necesita mantener un control preciso de sus existencias: saber cuántas unidades hay disponibles, registrar las salidas por venta y detectar a tiempo los productos que se están agotando para reponerlos.

**AppCappuCare** aborda esta necesidad con una aplicación Android que permite:

- Consultar rápidamente el catálogo y la disponibilidad de cada insumo.
- Registrar ventas, descontando automáticamente el stock vendido.
- Mantener un historial de movimientos (salidas por venta).
- Administrar los productos (crear, editar, eliminar).
- Recibir alertas visuales de productos **agotados** o con **stock bajo**.

Todos los datos utilizados son **ficticios**, conforme a las restricciones del caso. No se usan datos personales reales ni credenciales de sistemas productivos.

---

## Funcionalidades implementadas

### Autenticación
- Inicio de sesión con usuario y contraseña (usuarios de demostración).
- Recuperación de clave por nombre de usuario o correo.
- Cierre de sesión y perfil de usuario (nombre, rol, correo) desde el menú lateral.

### Catálogo de productos
- Listado de insumos médicos con búsqueda por nombre.
- Ordenamiento por nombre (A-Z / Z-A) y por precio (mayor a menor / menor a mayor).
- Detalle del producto: galería de imágenes, precio, código, descripción y estado de stock.
- Agregar productos al carrito directamente desde el catálogo.

### Favoritos
- Marcar y desmarcar productos favoritos.
- Pantalla dedicada con los productos favoritos, persistidos en la base de datos local.

### Carrito y ventas
- Carrito con aumento, disminución y eliminación de ítems.
- **Validación de stock**: no permite vender más unidades que las disponibles ni productos agotados.
- Asociación de la venta a un cliente (por defecto "Venta Mostrador").
- Simulación del proceso de pago con indicador de carga.
- Generación de **folio de venta** (`VTA-XXXX`).
- **Descuento automático del stock** al confirmar la venta y registro de movimientos de **SALIDA** (con producto, cantidad, fecha, motivo y cliente).

### Gestión de productos (CRUD)
- Crear, editar y eliminar productos (con diálogo de confirmación).
- Campos: nombre, código, descripción, precio, stock, unidad de medida y nivel mínimo.
- **Cálculo automático del estado de stock**: `DISPONIBLE`, `STOCK_BAJO` o `AGOTADO` según el nivel mínimo.
- Captura de **foto con la cámara** del dispositivo (con `FileProvider`).
- **Compartir** la información de un producto mediante el selector de apps de Android.
- Búsqueda por nombre o código.

### Alertas de reposición
- Centro de notificaciones con los productos agotados y con stock bajo.
- Burbuja flotante de alerta con acceso rápido al centro de notificaciones.
- Interruptor para activar o desactivar las notificaciones de stock bajo.

### Apariencia
- Tema **claro**, **oscuro** o **definido por el dispositivo**, seleccionable desde Ajustes.

### Persistencia local
- Base de datos **SQLite** (`appcappucare.db`) con precarga de **20 productos ficticios** y favoritos iniciales.

---

## Alcance pendiente / mejoras futuras

- Lectura de códigos **QR / códigos de barras** con la cámara (el botón existe en la pantalla de venta rápida, pero aún no está conectado).
- Persistencia del historial de movimientos y de las ventas en la base de datos (actualmente el historial se mantiene en memoria durante la sesión).
- Registro de movimientos de **ENTRADA** (recepción de mercadería) desde la interfaz.
- Sincronización con Firebase Firestore (la dependencia está configurada, pero aún no se utiliza).
- Autenticación real (hoy los usuarios son datos de demostración dentro de la app).
- Notificaciones push del sistema para stock mínimo.
- Integración con procesos de ventas, compras y facturación (etapa posterior, no obligatoria para el MVP).


##  Pasos para ejecutar

### Requisitos previos
- **Android Studio** reciente (el proyecto usa AGP 9.3.3, por lo que necesita una versión actualizada del IDE).
- **Android SDK** con la plataforma API 36 instalada.
- Un emulador Android (recomendado **API 35 o 36** con Google Play) o un dispositivo físico con depuración USB activada.
- Conexión a internet para la primera sincronización de Gradle.

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/BlorexInk/appcappucare.git
   ```
2. **Abrir el proyecto** en Android Studio: `File > Open` y seleccionar la carpeta `appcappucare`.
3. **Esperar la sincronización de Gradle** (puede tardar unos minutos la primera vez).
4. Verificar que el archivo `app/google-services.json` esté presente.
5. **Crear o seleccionar un emulador** en `Tools > Device Manager` (ej: Pixel 8 Pro, API 35, Google Play).
6. Presionar **Run** (`Shift + F10`) y esperar a que la app se instale y se abra.

### Credenciales de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `blorex` | `1234` | Administrador |
| `admin` | `admin123` | Gerente de Inventario |
| `vendedor` | `pass123` | Vendedor Mostrador |