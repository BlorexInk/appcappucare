/**
 * Asistente de compras con IA para AppCappuCare.
 *
 * Expone:
 *   GET  /api/ia/estado  -> indica qué proveedor de IA está configurado.
 *   POST /api/ia/chat    -> responde preguntas usando SOLO el catálogo que envía la app.
 *
 * Proveedores soportados (se elige con IA_PROVIDER en backend/.env):
 *   - gemini  (Google, tiene nivel gratuito)   -> GEMINI_API_KEY
 *   - claude  (Anthropic, de pago por uso)     -> ANTHROPIC_API_KEY
 *
 * La clave de la API vive SOLO aquí, en el servidor. Nunca va dentro de la app.
 * Requiere Node 18 o superior (usa fetch nativo). No agrega dependencias nuevas.
 */

const fs = require('fs');
const path = require('path');

// ---------- Lector mínimo de backend/.env (sin instalar dotenv) ----------
function cargarEnv() {
    const ruta = path.join(__dirname, '.env');
    if (!fs.existsSync(ruta)) return;
    for (const linea of fs.readFileSync(ruta, 'utf8').split(/\r?\n/)) {
        const m = linea.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
        if (!m || linea.trim().startsWith('#')) continue;
        if (process.env[m[1]] === undefined) {
            process.env[m[1]] = m[2].replace(/^["']|["']$/g, '');
        }
    }
}
cargarEnv();

const PROVEEDOR = (process.env.IA_PROVIDER || 'gemini').toLowerCase();
// Los nombres de modelo cambian con el tiempo: si uno deja de existir, cámbialo en .env.
const MODELO_GEMINI = process.env.GEMINI_MODEL || 'gemini-flash-latest';
// Si el modelo principal está saturado o no responde, se prueba este otro (más liviano).
const MODELO_GEMINI_RESPALDO = process.env.GEMINI_FALLBACK_MODEL || 'gemini-flash-lite-latest';
const MODELO_CLAUDE = process.env.CLAUDE_MODEL || 'claude-haiku-5-5';
// Solo para pruebas locales (servidor simulado). En producción déjalo vacío.
const URL_BASE_IA = process.env.IA_BASE_URL || '';

const MAX_MENSAJE = 600;
const MAX_HISTORIAL = 6;
const MAX_CATALOGO = 200;
const TIMEOUT_MS = Number(process.env.IA_TIMEOUT_MS) || 25000;

// ---------- Límite simple de uso: 20 consultas por minuto por IP ----------
const consultas = new Map();
function limitar(req, res, next) {
    const ip = req.ip || 'desconocida';
    const ahora = Date.now();
    const recientes = (consultas.get(ip) || []).filter(t => ahora - t < 60000);
    if (recientes.length >= 20) {
        return res.status(429).json({ error: 'Demasiadas consultas seguidas. Espera un minuto e intenta de nuevo.' });
    }
    recientes.push(ahora);
    consultas.set(ip, recientes);
    next();
}

// ---------- Prompt del sistema ----------
function construirSistema(catalogo) {
    const lineas = catalogo.map(p =>
        `${p.codigo} | ${p.nombre} | ${(p.descripcion || '').slice(0, 220)} | $${p.valor} | ${p.estadoStock} (${p.cantidadDisponible} u.)`
    ).join('\n');

    return `Eres el asistente de compras de AppCappuCare, una tienda de insumos médicos hospitalarios en Chile.
Hablas en español de Chile, de forma breve, clara y amable.

PUEDES AYUDAR CON:
1) Buscar productos EQUIVALENTES o alternativos dentro del catálogo (misma función o uso).
2) Armar LISTAS DE COMPRA o kits según una especialidad o procedimiento (ej: kit de curación, kit de inyectables).
3) RESUMIR la descripción o ficha de un producto del catálogo en pocas líneas.

REGLAS ESTRICTAS:
- Recomienda SOLO productos del CATÁLOGO de abajo, usando su código exacto. Nunca inventes productos ni códigos.
- Si un producto está AGOTADO, dilo y sugiere una alternativa disponible si existe.
- Resume usando únicamente la descripción que aparece en el catálogo; si falta información, dilo. No inventes datos técnicos.
- No des diagnósticos, dosis ni indicaciones clínicas. Si te las piden, responde que eso lo debe indicar un profesional de salud.
- Si la consulta no tiene relación con insumos médicos o con este catálogo, explica amablemente qué puedes hacer.
- En las listas, usa cantidades razonables (normalmente entre 1 y 10).

FORMATO DE RESPUESTA: responde SOLO con un objeto JSON válido, sin texto antes ni después ni bloques de código:
{"respuesta": "texto para el usuario", "productos": [{"codigo": "CODIGO", "cantidad": 1}]}
Si no recomiendas productos, usa "productos": [].

CATÁLOGO (código | nombre | descripción | precio CLP | estado):
${lineas}`;
}

// ---------- Llamadas a los proveedores ----------
async function conTimeout(url, opciones) {
    const control = new AbortController();
    const temporizador = setTimeout(() => control.abort(), TIMEOUT_MS);
    try {
        return await fetch(url, { ...opciones, signal: control.signal });
    } catch (e) {
        if (e.name === 'AbortError') {
            console.error(`[IA] ${PROVEEDOR} no respondió en ${TIMEOUT_MS / 1000}s (modelo: ${PROVEEDOR === 'claude' ? MODELO_CLAUDE : MODELO_GEMINI}).`);
            const err = new ErrorIA(503, 'La IA tardó demasiado en responder. Intenta de nuevo.', true);
            err.esTimeout = true;
            throw err;
        }
        console.error(`[IA] No se pudo conectar con ${PROVEEDOR}: ${e.cause?.code || e.message}`);
        throw e;
    } finally {
        clearTimeout(temporizador);
    }
}

async function llamarGemini(sistema, mensajes, modelo = MODELO_GEMINI) {
    const clave = process.env.GEMINI_API_KEY;
    if (!clave) throw new ErrorIA(503, 'Falta GEMINI_API_KEY en backend/.env');
    const base = URL_BASE_IA || 'https://generativelanguage.googleapis.com';
    const r = await conTimeout(`${base}/v1beta/models/${modelo}:generateContent`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'x-goog-api-key': clave },
        body: JSON.stringify({
            systemInstruction: { parts: [{ text: sistema }] },
            contents: mensajes.map(m => ({
                role: m.rol === 'asistente' ? 'model' : 'user',
                parts: [{ text: m.texto }]
            })),
            generationConfig: { temperature: 0.3, responseMimeType: 'application/json' }
        })
    });
    if (!r.ok) throw await errorDelProveedor(r);
    const datos = await r.json();
    const partes = datos?.candidates?.[0]?.content?.parts || [];
    return partes.map(p => p.text || '').join('');
}

async function llamarClaude(sistema, mensajes) {
    const clave = process.env.ANTHROPIC_API_KEY;
    if (!clave) throw new ErrorIA(503, 'Falta ANTHROPIC_API_KEY en backend/.env');
    const base = URL_BASE_IA || 'https://api.anthropic.com';
    const r = await conTimeout(`${base}/v1/messages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'x-api-key': clave,
            'anthropic-version': '2023-06-01'
        },
        body: JSON.stringify({
            model: MODELO_CLAUDE,
            max_tokens: 1024,
            system: sistema,
            messages: mensajes.map(m => ({
                role: m.rol === 'asistente' ? 'assistant' : 'user',
                content: m.texto
            }))
        })
    });
    if (!r.ok) throw await errorDelProveedor(r);
    const datos = await r.json();
    return (datos?.content || []).filter(b => b.type === 'text').map(b => b.text).join('');
}

class ErrorIA extends Error {
    constructor(estado, mensaje, reintentable = false) {
        super(mensaje);
        this.estado = estado;
        this.reintentable = reintentable;
    }
}

const esperar = ms => new Promise(r => setTimeout(r, ms));

// Convierte una respuesta de error del proveedor en un ErrorIA claro y deja el detalle en la terminal.
async function errorDelProveedor(r) {
    let detalle = '';
    try { detalle = (await r.text()).replace(/\s+/g, ' ').slice(0, 400); } catch (_) { /* sin cuerpo */ }
    console.error(`[IA] ${PROVEEDOR} respondió ${r.status}: ${detalle}`);
    if (r.status === 429) {
        return new ErrorIA(429, 'Se alcanzó el límite de uso de la IA. Espera un minuto e intenta de nuevo.', true);
    }
    if (r.status >= 500) {
        return new ErrorIA(503, 'La IA está saturada en este momento. Intenta de nuevo en unos segundos.', true);
    }
    return new ErrorIA(502, `La IA rechazó la solicitud (${r.status}). Revisa la clave y el nombre del modelo en backend/.env.`);
}

// Reintenta hasta 3 veces (esperando 1,5 s y 3 s) cuando el proveedor está saturado o limita el uso.
async function conReintentos(funcion) {
    const pausas = [1500, 3000];
    for (let intento = 0; ; intento++) {
        try {
            return await funcion();
        } catch (e) {
            if (!(e instanceof ErrorIA) || !e.reintentable || intento >= pausas.length) throw e;
            // Un tiempo de espera agotado se reintenta solo una vez (para no dejar al usuario esperando minutos).
            if (e.esTimeout && intento >= 1) throw e;
            console.error(`[IA] Reintentando (${intento + 1}/${pausas.length})...`);
            await esperar(pausas[intento]);
        }
    }
}

// ---------- Procesar la salida del modelo ----------
function extraerJson(texto) {
    const limpio = String(texto || '').replace(/```json|```/gi, '').trim();
    try { return JSON.parse(limpio); } catch (_) { /* intenta recortar */ }
    const ini = limpio.indexOf('{');
    const fin = limpio.lastIndexOf('}');
    if (ini >= 0 && fin > ini) {
        try { return JSON.parse(limpio.slice(ini, fin + 1)); } catch (_) { /* sin JSON */ }
    }
    return null;
}

function validarCatalogo(entrada) {
    if (!Array.isArray(entrada) || entrada.length === 0) return null;
    return entrada.slice(0, MAX_CATALOGO).map(p => ({
        codigo: String(p.codigo ?? '').slice(0, 40),
        nombre: String(p.nombre ?? '').slice(0, 120),
        descripcion: String(p.descripcion ?? ''),
        valor: Number(p.valor) || 0,
        cantidadDisponible: Number.isFinite(Number(p.cantidadDisponible)) ? Number(p.cantidadDisponible) : 0,
        estadoStock: String(p.estadoStock ?? 'DISPONIBLE').slice(0, 20)
    })).filter(p => p.codigo && p.nombre);
}

// ---------- Memoria de respuestas (evita llamar a la IA para la misma consulta) ----------
const crypto = require('crypto');
const cacheRespuestas = new Map();
const CACHE_MAX = 100;
const CACHE_MS = 30 * 60 * 1000;

function claveCache(productos, mensaje) {
    const firma = productos.map(p => `${p.codigo}:${p.valor}:${p.cantidadDisponible}`).join('|');
    return crypto.createHash('md5')
        .update(`${PROVEEDOR}|${firma}|${mensaje.trim().toLowerCase()}`)
        .digest('hex');
}

// Llama al modelo principal (con reintentos) y, si sigue fallando por saturación, al de respaldo.
async function obtenerRespuesta(sistema, conversacion) {
    if (PROVEEDOR === 'claude') {
        return conReintentos(() => llamarClaude(sistema, conversacion));
    }
    try {
        return await conReintentos(() => llamarGemini(sistema, conversacion, MODELO_GEMINI));
    } catch (e) {
        const hayRespaldo = MODELO_GEMINI_RESPALDO && MODELO_GEMINI_RESPALDO !== MODELO_GEMINI;
        if (!(e instanceof ErrorIA) || !e.reintentable || !hayRespaldo) throw e;
        console.error(`[IA] Probando el modelo de respaldo: ${MODELO_GEMINI_RESPALDO}`);
        try {
            return await llamarGemini(sistema, conversacion, MODELO_GEMINI_RESPALDO);
        } catch (_) {
            throw e; // si el respaldo también falla, se informa el problema original
        }
    }
}

module.exports = function registrarRutasIA(app) {

    app.get('/api/ia/estado', (req, res) => {
        const clave = PROVEEDOR === 'claude' ? process.env.ANTHROPIC_API_KEY : process.env.GEMINI_API_KEY;
        res.json({ proveedor: PROVEEDOR, configurado: Boolean(clave) });
    });

    app.post('/api/ia/chat', limitar, async (req, res) => {
        try {
            if (typeof fetch !== 'function') {
                throw new ErrorIA(500, 'Este servidor necesita Node 18 o superior para usar la IA.');
            }
            if (PROVEEDOR !== 'gemini' && PROVEEDOR !== 'claude') {
                throw new ErrorIA(500, 'IA_PROVIDER debe ser "gemini" o "claude".');
            }

            const { mensaje, historial, catalogo } = req.body || {};
            if (typeof mensaje !== 'string' || !mensaje.trim()) {
                throw new ErrorIA(400, 'Escribe una consulta.');
            }
            const productos = validarCatalogo(catalogo);
            if (!productos) throw new ErrorIA(400, 'La app no envió el catálogo.');

            const previos = (Array.isArray(historial) ? historial : [])
                .slice(-MAX_HISTORIAL)
                .filter(h => h && typeof h.texto === 'string' && h.texto.trim())
                .map(h => ({
                    rol: h.rol === 'asistente' ? 'asistente' : 'usuario',
                    texto: h.texto.slice(0, MAX_MENSAJE)
                }));
            // Los proveedores exigen que la conversación parta con un mensaje del usuario.
            while (previos.length && previos[0].rol !== 'usuario') previos.shift();
            const conversacion = [...previos, { rol: 'usuario', texto: mensaje.trim().slice(0, MAX_MENSAJE) }];

            // Consulta repetida (sin historial previo): se responde desde la memoria.
            const usarCache = previos.length === 0;
            const clave = usarCache ? claveCache(productos, mensaje) : null;
            if (usarCache) {
                const guardada = cacheRespuestas.get(clave);
                if (guardada && Date.now() - guardada.hora < CACHE_MS) {
                    return res.json({ ...guardada.datos, desdeMemoria: true });
                }
            }

            const sistema = construirSistema(productos);
            const bruto = await obtenerRespuesta(sistema, conversacion);

            const json = extraerJson(bruto);
            const respuesta = (json && typeof json.respuesta === 'string' && json.respuesta.trim())
                ? json.respuesta.trim()
                : 'No pude armar una respuesta clara. ¿Puedes reformular tu consulta?';

            // Solo se aceptan códigos que existan de verdad en el catálogo recibido.
            const porCodigo = new Map(productos.map(p => [p.codigo.toLowerCase(), p]));
            const vistos = new Set();
            const sugeridos = [];
            for (const item of (json && Array.isArray(json.productos) ? json.productos : [])) {
                const p = porCodigo.get(String(item?.codigo ?? '').toLowerCase());
                if (!p || vistos.has(p.codigo)) continue;
                vistos.add(p.codigo);
                const cantidad = Math.min(50, Math.max(1, parseInt(item.cantidad, 10) || 1));
                sugeridos.push({
                    codigo: p.codigo, nombre: p.nombre, valor: p.valor,
                    estadoStock: p.estadoStock, cantidad
                });
            }

            const datos = { respuesta, productos: sugeridos, proveedor: PROVEEDOR };
            // Solo se guardan respuestas válidas (no el mensaje de "no pude armar una respuesta").
            if (usarCache && json && typeof json.respuesta === 'string') {
                if (cacheRespuestas.size >= CACHE_MAX) cacheRespuestas.delete(cacheRespuestas.keys().next().value);
                cacheRespuestas.set(clave, { hora: Date.now(), datos });
            }
            res.json(datos);
        } catch (e) {
            if (e instanceof ErrorIA) return res.status(e.estado).json({ error: e.message });
            console.error('[IA] Error:', e.name === 'AbortError' ? 'tiempo de espera agotado' : e.message);
            const motivo = e.name === 'AbortError'
                ? 'La IA tardó demasiado en responder. Intenta de nuevo.'
                : 'No se pudo contactar a la IA. Revisa la conexión a internet del servidor.';
            res.status(503).json({ error: motivo });
        }
    });
};