# CappuCare

**CappuCare** es una aplicación móvil Android orientada a la gestión y consulta de un catálogo de insumos médicos. Integra funciones de inventario, carrito de compras y un asistente de inteligencia artificial que ayuda a encontrar productos y obtener recomendaciones a partir del catálogo.

## Funcionalidades

- Consulta y administración de productos del inventario.
- Creación, edición y eliminación de productos mediante el backend.
- Carrito de compras con opción de agregar productos recomendados.
- Asistente de compras con IA integrado con Gemini.
- Recomendaciones basadas en los productos del catálogo.
- Persistencia local mediante Room y comunicación con un backend Spring Boot.

El repositorio también contiene módulos y pantallas para otras funciones de la aplicación, como usuarios, favoritos, ventas y solicitudes. Su funcionamiento debe verificarse según el entorno de ejecución.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Aplicación móvil | Android, Kotlin |
| Interfaz | Jetpack Compose / Material 3 |
| Persistencia local | Room |
| Backend principal | Java 17, Spring Boot 3.3.13 |
| API de inteligencia artificial | Google Gemini |
| Compilación Android | Gradle |
| Compilación backend | Maven |
| Control de versiones | Git y GitHub |

## Estructura del repositorio

```text
app/                 Aplicación Android
backend-springboot/  Backend principal en Spring Boot
backend/             Backend Node.js anterior / archivos heredados
gradle/              Configuración de Gradle
```

## Requisitos

- Android Studio y un emulador Android o dispositivo compatible.
- JDK 17.
- Maven disponible en la terminal.
- Conexión a internet para utilizar Gemini.
- Acceso a la configuración y a los servicios de persistencia requeridos por el backend.
- Clave válida de la API de Gemini para activar el asistente.

## Ejecutar el backend Spring Boot

Desde la raíz del repositorio, abrir PowerShell:

```powershell
cd .\backend-springboot
$env:GEMINI_API_KEY = "TU_CLAVE_DE_GEMINI"
$env:GEMINI_MODEL = "gemini-2.5-flash-lite"
mvn spring-boot:run
```

> **Seguridad:** nunca subir una clave real, un archivo `.env` con secretos ni credenciales al repositorio. El archivo `.env.example`, si existe, debe contener únicamente valores de ejemplo. Las variables de entorno definidas como arriba se mantienen en la sesión actual de PowerShell.

Para comprobar la configuración del asistente, en otra terminal:

```powershell
Invoke-RestMethod -Uri "http://localhost:3000/api/ia/estado"
```

La respuesta `configurado: true` indica que el backend detecta la clave; no garantiza por sí sola que Gemini esté disponible.

## Ejecutar la aplicación Android

1. Abrir la carpeta raíz del repositorio en Android Studio.
2. Esperar la sincronización de Gradle.
3. Iniciar un emulador Android.
4. Ejecutar la configuración **app**.
5. Comprobar que el backend Spring Boot esté activo.

En el emulador Android, la dirección `http://10.0.2.2:3000` permite acceder al backend que se ejecuta en el computador anfitrión. `localhost:3000` se utiliza para probarlo desde el propio computador. En un dispositivo físico se debe configurar una dirección de red accesible.

## Endpoints del asistente IA

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/ia/estado` | Consulta si el proveedor de IA está configurado. |
| POST | `/api/ia/chat` | Recibe la consulta y el catálogo y devuelve una respuesta con posibles recomendaciones. |

## Pruebas

Para compilar el backend:

```powershell
cd .\backend-springboot
mvn clean compile
```

Para ejecutar las pruebas del backend:

```powershell
mvn test
```

También se recomienda verificar manualmente el flujo de inventario (crear, consultar, editar y eliminar), las recomendaciones del asistente y la incorporación de productos al carrito.

## Preparación para la presentación

- Ejecutar Spring Boot y comprobar la conexión desde Android.
- Demostrar el CRUD de productos.
- Consultar al asistente IA y agregar una recomendación al carrito.
- Ejecutar y explicar las pruebas automatizadas.
- Mostrar la estructura del proyecto, los commits de GitHub y la planificación de Trello.
- Generar y mostrar un APK firmado, si lo exige la evaluación.

## Seguridad y configuración local

Antes de realizar un commit, revisar los archivos que Git va a incluir. Excluir credenciales, archivos `.env`, `local.properties`, almacenes de claves de firma (`*.jks`, `*.keystore`) y bases de datos locales con información privada. No publicar claves de Gemini ni contraseñas.

---

**Proyecto académico — CappuCare**
