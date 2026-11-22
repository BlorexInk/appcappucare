package cl.cappucare.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ia")
public class IaController {

    private static final String MODELO_PRINCIPAL = "gemini-flash-latest";
    private static final String MODELO_RESPALDO = "gemini-flash-lite-latest";
    private static final long[] ESPERAS_MS = {1500L, 3000L};

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @GetMapping("/estado")
    public Map<String, Object> estado() {
        String clave = System.getenv("GEMINI_API_KEY");
        return Map.of("proveedor", "gemini", "configurado", clave != null && !clave.isBlank());
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody JsonNode solicitud) {
        String clave = System.getenv("GEMINI_API_KEY");
        if (clave == null || clave.isBlank()) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "Falta configurar GEMINI_API_KEY en Spring Boot.");
        }

        String mensaje = solicitud.path("mensaje").asText("").trim();
        JsonNode catalogo = solicitud.path("catalogo");
        JsonNode historial = solicitud.path("historial");
        if (mensaje.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Escribe una consulta.");
        }
        if (!catalogo.isArray() || catalogo.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "La aplicación no envió el catálogo.");
        }

        try {
            Map<String, JsonNode> productosPorCodigo = new HashMap<>();
            StringBuilder listado = new StringBuilder();
            int limite = Math.min(catalogo.size(), 200);
            for (int i = 0; i < limite; i++) {
                JsonNode p = catalogo.get(i);
                String codigo = p.path("codigo").asText("").trim();
                String nombre = p.path("nombre").asText("").trim();
                if (codigo.isBlank() || nombre.isBlank()) continue;
                productosPorCodigo.put(codigo.toLowerCase(Locale.ROOT), p);
                String descripcion = p.path("descripcion").asText("");
                if (descripcion.length() > 220) descripcion = descripcion.substring(0, 220);
                listado.append(codigo).append(" | ").append(nombre).append(" | ")
                        .append(descripcion).append(" | $").append(p.path("valor").asDouble())
                        .append(" | ").append(p.path("estadoStock").asText("DISPONIBLE"))
                        .append(" | Stock: ").append(p.path("cantidadDisponible").asInt())
                        .append('\n');
            }
            if (productosPorCodigo.isEmpty()) {
                return error(HttpStatus.BAD_REQUEST, "El catálogo no contiene productos válidos.");
            }

            String sistema = """
                    Eres el asistente de compras de CappuCare, una tienda de insumos médicos en Chile.
                    Responde en español de Chile, de forma breve y clara.
                    Ayuda a buscar equivalentes, armar kits de compra y resumir fichas del catálogo.
                    REGLAS:
                    - Recomienda SOLO productos del catálogo y utiliza sus códigos exactos.
                    - Nunca inventes productos, códigos ni características técnicas.
                    - Si algo está agotado, indícalo.
                    - No des diagnósticos, dosis ni indicaciones clínicas.
                    - Si la pregunta no corresponde al catálogo, explica en qué puedes ayudar.
                    - Para kits usa cantidades razonables.
                    Responde SOLO un objeto JSON válido con esta estructura:
                    {"respuesta":"texto para el usuario","productos":[{"codigo":"CODIGO","cantidad":1}]}
                    Si no recomiendas productos, usa "productos": [].
                    CATÁLOGO (código | nombre | descripción | precio CLP | estado | stock):
                    """ + listado;

            ObjectNode cuerpo = mapper.createObjectNode();
            ObjectNode instruccion = mapper.createObjectNode();
            instruccion.putArray("parts").addObject().put("text", sistema);
            cuerpo.set("systemInstruction", instruccion);
            ArrayNode conversacion = cuerpo.putArray("contents");

            if (historial.isArray()) {
                int inicio = Math.max(0, historial.size() - 6);
                boolean primerTurno = true;
                for (int i = inicio; i < historial.size(); i++) {
                    JsonNode h = historial.get(i);
                    String texto = h.path("texto").asText("").trim();
                    if (texto.isEmpty()) continue;
                    String rol = "asistente".equals(h.path("rol").asText()) ? "model" : "user";
                    if (primerTurno && "model".equals(rol)) continue;
                    primerTurno = false;
                    conversacion.addObject().put("role", rol)
                            .putArray("parts").addObject()
                            .put("text", texto.substring(0, Math.min(600, texto.length())));
                }
            }
            conversacion.addObject().put("role", "user")
                    .putArray("parts").addObject()
                    .put("text", mensaje.substring(0, Math.min(600, mensaje.length())));
            ObjectNode generacion = mapper.createObjectNode();
            generacion.put("temperature", 0.3);
            generacion.put("responseMimeType", "application/json");
            cuerpo.set("generationConfig", generacion);

            String principal = variable("GEMINI_MODEL", MODELO_PRINCIPAL);
            String respaldo = variable("GEMINI_FALLBACK_MODEL", MODELO_RESPALDO);
            String respuestaBruta;
            try {
                respuestaBruta = llamarConReintentos(principal, clave, cuerpo);
            } catch (ErrorGemini fallo) {
                if (!fallo.reintentable || respaldo.equals(principal) || respaldo.isBlank()) {
                    throw fallo;
                }
                System.err.println("[IA] Probando modelo de respaldo: " + respaldo);
                respuestaBruta = llamarConReintentos(respaldo, clave, cuerpo);
            }

            String limpio = respuestaBruta.trim().replace("```json", "").replace("```", "").trim();
            JsonNode resultado = mapper.readTree(limpio);
            String respuesta = resultado.path("respuesta").asText("").trim();
            if (respuesta.isEmpty()) respuesta = "No pude generar una respuesta clara. Intenta reformular la pregunta.";

            ArrayNode sugeridos = mapper.createArrayNode();
            Set<String> vistos = new HashSet<>();
            JsonNode recomendaciones = resultado.path("productos");
            if (recomendaciones.isArray()) {
                for (JsonNode r : recomendaciones) {
                    String codigo = r.path("codigo").asText("").toLowerCase(Locale.ROOT);
                    JsonNode producto = productosPorCodigo.get(codigo);
                    if (producto == null || !vistos.add(codigo)) continue;
                    ObjectNode sugerido = sugeridos.addObject();
                    sugerido.put("codigo", producto.path("codigo").asText());
                    sugerido.put("nombre", producto.path("nombre").asText());
                    sugerido.put("valor", producto.path("valor").asDouble());
                    sugerido.put("estadoStock", producto.path("estadoStock").asText("DISPONIBLE"));
                    sugerido.put("cantidad", Math.max(1, Math.min(50, r.path("cantidad").asInt(1))));
                }
            }
            return ResponseEntity.ok(Map.of("respuesta", respuesta, "productos", sugeridos, "proveedor", "gemini"));
        } catch (ErrorGemini e) {
            return error(HttpStatus.valueOf(e.estado), e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return error(HttpStatus.SERVICE_UNAVAILABLE, "Se interrumpió la consulta a Gemini.");
        } catch (Exception e) {
            System.err.println("[IA] Error: " + e.getMessage());
            return error(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo obtener una respuesta válida de Gemini. Revisa la consola de Spring Boot.");
        }
    }

    private String llamarConReintentos(String modelo, String clave, JsonNode cuerpo)
            throws Exception {
        for (int intento = 0; ; intento++) {
            try {
                return llamarGemini(modelo, clave, cuerpo);
            } catch (ErrorGemini e) {
                if (!e.reintentable || intento >= ESPERAS_MS.length) throw e;
                System.err.println("[IA] " + modelo + " temporalmente no disponible. Reintento "
                        + (intento + 1) + "/" + ESPERAS_MS.length);
                Thread.sleep(ESPERAS_MS[intento]);
            }
        }
    }

    private String llamarGemini(String modelo, String clave, JsonNode cuerpo)
            throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelo + ":generateContent";
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", clave)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new ErrorGemini(503, "Gemini tardó demasiado en responder.", true);
        }

        int codigo = respuesta.statusCode();
        if (codigo < 200 || codigo >= 300) {
            System.err.println("[IA] Gemini " + modelo + " HTTP " + codigo + ": "
                    + respuesta.body().substring(0, Math.min(500, respuesta.body().length())));
            if (codigo == 429) {
                throw new ErrorGemini(429, "Se alcanzó el límite de uso de Gemini. Intenta nuevamente en unos minutos.", true);
            }
            if (codigo >= 500) {
                throw new ErrorGemini(503, "Gemini está saturado temporalmente. Intenta de nuevo.", true);
            }
            throw new ErrorGemini(502, "Gemini rechazó la solicitud (HTTP " + codigo
                    + "). Revisa el modelo y la clave API.", false);
        }

        JsonNode datos = mapper.readTree(respuesta.body());
        JsonNode partes = datos.path("candidates").path(0).path("content").path("parts");
        StringBuilder texto = new StringBuilder();
        if (partes.isArray()) {
            for (JsonNode parte : partes) texto.append(parte.path("text").asText(""));
        }
        if (texto.toString().isBlank()) {
            throw new ErrorGemini(503, "Gemini no devolvió contenido. Intenta de nuevo.", true);
        }
        return texto.toString();
    }

    private String variable(String nombre, String defecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }

    private static class ErrorGemini extends Exception {
        final int estado;
        final boolean reintentable;
        ErrorGemini(int estado, String mensaje, boolean reintentable) {
            super(mensaje);
            this.estado = estado;
            this.reintentable = reintentable;
        }
    }
}
