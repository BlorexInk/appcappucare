# Backend Spring Boot de CappuCare

Java 17+ y Maven 3.9+. Desde esta carpeta ejecutar `mvn test` y `mvn spring-boot:run`.

**Importante:** detener Node.js antes: Spring Boot usa el mismo puerto 3000 y mantiene las URLs existentes de Android (`10.0.2.2:3000`). Copiar `../backend/database.db` a `./database.db` **con el servidor Node detenido** si se desea conservar los registros existentes. No ejecutar ambos servidores sobre el mismo archivo SQLite.

Endpoints: `GET/POST /api/medicamentos`, `GET /api/medicamentos/codigo/{codigo}`, `PUT/DELETE /api/medicamentos/{codigo}`, `GET /api/productos`, `GET /api/usuarios`, `GET/POST /api/configuracion`, `POST /api/sync`, `POST /api/upload`.

Comprobar: `curl http://localhost:3000/api/medicamentos`. Android Emulator usa 10.0.2.2; teléfono real requiere la IP LAN de tu computador en la configuración del servidor. El endpoint de IA anterior de Node.js **no fue migrado**. Verifica cualquier función de IA por separado.

La sincronización original incluye contraseñas en texto plano. Es un problema de seguridad heredado: **no usar datos personales reales ni publicar el servidor en Internet** sin implementar autenticación, hash y autorización.

Este proyecto necesita descargar dependencias Maven; la compilación debe verificarse en un equipo con Maven y acceso a repositorios.
