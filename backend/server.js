
/**
 * Servidor Backend REST API para AppCappuCare
 *
 * Tecnologías: Node.js, Express, SQLite3, Multer y CORS.
 */

const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const multer = require('multer');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

// Rutas para gestionar medicamentos
const crearRutasMedicamentos = require('./routes/medicamentos');

const app = express();
const PORT = process.env.PORT || 3000;

// ==========================================
// CONFIGURACIÓN GENERAL
// ==========================================

app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// ==========================================
// CONFIGURACIÓN DE IMÁGENES
// ==========================================

const uploadsDir = path.join(__dirname, 'uploads');

if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir, { recursive: true });
}

app.use('/uploads', express.static(uploadsDir));

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, uploadsDir);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        const ext = path.extname(file.originalname) || '.jpg';
        cb(null, 'img-' + uniqueSuffix + ext);
    }
});

const upload = multer({ storage: storage });

// ==========================================
// CONEXIÓN CON SQLITE
// ==========================================

const dbPath = path.join(__dirname, 'database.db');

const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error al conectar con la base de datos:', err.message);
    } else {
        console.log('Base de datos SQLite conectada exitosamente en:', dbPath);
    }
});

// ==========================================
// INICIALIZACIÓN DE TABLAS
// ==========================================

db.serialize(() => {

    db.run(`
        CREATE TABLE IF NOT EXISTS productos (
            codigo TEXT PRIMARY KEY,
            nombre TEXT NOT NULL,
            descripcion TEXT,
            valor REAL NOT NULL,
            cantidadDisponible INTEGER NOT NULL,
            unidadMedida TEXT NOT NULL,
            nivelMinimo INTEGER NOT NULL,
            estadoStock TEXT NOT NULL,
            imagenes TEXT
        )
    `);

    db.run(`
        CREATE TABLE IF NOT EXISTS usuarios (
            username TEXT PRIMARY KEY,
            password TEXT NOT NULL,
            nombreCompleto TEXT NOT NULL,
            rol TEXT NOT NULL,
            categoriaCargo TEXT NOT NULL,
            email TEXT NOT NULL,
            telefono TEXT,
            direccion TEXT,
            ubicacionBodega TEXT,
            fotoPerfilUrl TEXT,
            fechaRegistro TEXT,
            horaInicioSesion TEXT
        )
    `);

    db.run(`
        CREATE TABLE IF NOT EXISTS configuracion (
            clave TEXT PRIMARY KEY,
            valor TEXT
        )
    `);
});

// ==========================================
// ENDPOINTS REST API - MEDICAMENTOS
// ==========================================

app.use(
    '/api/medicamentos',
    crearRutasMedicamentos(db)
);

// ==========================================
// ENDPOINTS REST API - PRODUCTOS Y SUBIDA
// ==========================================

app.get('/api/productos', (req, res) => {

    db.all('SELECT * FROM productos', [], (err, rows) => {

        if (err) {
            return res.status(500).json({ error: err.message });
        }

        const productos = rows.map(r => ({
            ...r,
            imagenes: r.imagenes ? r.imagenes.split(';') : []
        }));

        res.json({ productos });
    });
});

app.post('/api/upload', upload.single('imagen'), (req, res) => {

    if (!req.file) {
        return res.status(400).json({
            error: 'No se envió ningún archivo de imagen'
        });
    }

    const host = req.get('host');
    const protocol = req.protocol;

    const imageUrl = `${protocol}://${host}/uploads/${req.file.filename}`;

    console.log(`[UPLOAD] Foto subida al servidor: ${imageUrl}`);

    res.json({
        url: imageUrl,
        filename: req.file.filename
    });
});

// ==========================================
// SINCRONIZACIÓN DE PRODUCTOS Y USUARIOS
// ==========================================

app.post('/api/sync', (req, res) => {

    const { productos, usuarios, configuracion } = req.body;

    console.log('[SYNC] Sincronizando productos y usuarios con el servidor...');

    db.serialize(() => {

        // 1. Sincronizar productos
        if (Array.isArray(productos)) {

            const stmtP = db.prepare(`
                INSERT OR REPLACE INTO productos
                (codigo, nombre, descripcion, valor, cantidadDisponible, unidadMedida, nivelMinimo, estadoStock, imagenes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            `);

            for (const p of productos) {

                const imagenesStr = Array.isArray(p.imagenes)
                    ? p.imagenes.join(';')
                    : (p.imagenes || '');

                stmtP.run(
                    p.codigo,
                    p.nombre,
                    p.descripcion || '',
                    p.valor || 0,
                    p.cantidadDisponible || 0,
                    p.unidadMedida || 'un',
                    p.nivelMinimo || 10,
                    p.estadoStock || 'DISPONIBLE',
                    imagenesStr
                );
            }

            stmtP.finalize();
        }

        // 2. Sincronizar usuarios registrados
        if (Array.isArray(usuarios)) {

            const stmtU = db.prepare(`
                INSERT OR REPLACE INTO usuarios
                (username, password, nombreCompleto, rol, categoriaCargo, email, telefono, direccion, ubicacionBodega, fotoPerfilUrl, fechaRegistro, horaInicioSesion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `);

            for (const u of usuarios) {

                stmtU.run(
                    u.username,
                    u.password || '1234',
                    u.nombreCompleto || u.username,
                    u.rol || 'Vendedor',
                    u.categoriaCargo || 'Ventas y Mostrador',
                    u.email || '',
                    u.telefono || '',
                    u.direccion || '',
                    u.ubicacionBodega || 'Bodega Central XXXX',
                    u.fotoPerfilUrl || '',
                    u.fechaRegistro || '20/10/2026',
                    u.horaInicioSesion || 'Sesión Inactiva'
                );
            }

            stmtU.finalize();
        }

        // 3. Sincronizar marca y colores
        if (configuracion) {

            if (configuracion.logoAppUri) {
                db.run(
                    'INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)',
                    ['logoAppUri', configuracion.logoAppUri]
                );
            }

            if (configuracion.gamaColor) {
                db.run(
                    'INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)',
                    ['gamaColor', configuracion.gamaColor]
                );
            }
        }

        // 4. Retornar información sincronizada
        db.all('SELECT * FROM productos', [], (err, rowsP) => {

            if (err) {
                return res.status(500).json({ error: err.message });
            }

            const prods = rowsP.map(r => ({
                ...r,
                imagenes: r.imagenes ? r.imagenes.split(';') : []
            }));

            db.all('SELECT * FROM usuarios', [], (err2, rowsU) => {

                if (err2) {
                    return res.status(500).json({ error: err2.message });
                }

                db.all('SELECT * FROM configuracion', [], (err3, rowsC) => {

                    let configObj = {};

                    if (!err3 && rowsC) {
                        rowsC.forEach(c => configObj[c.clave] = c.valor);
                    }

                    res.json({
                        mensaje: 'Sincronización exitosa de productos, usuarios y configuración',
                        totalProductos: prods.length,
                        totalUsuarios: rowsU.length,
                        productos: prods,
                        usuarios: rowsU,
                        configuracion: configObj
                    });
                });
            });
        });
    });
});

// ==========================================
// ENDPOINTS REST API - USUARIOS Y CONFIG
// ==========================================

app.get('/api/usuarios', (req, res) => {

    db.all('SELECT * FROM usuarios', [], (err, rows) => {

        if (err) {
            return res.status(500).json({ error: err.message });
        }

        res.json({ usuarios: rows });
    });
});

app.get('/api/configuracion', (req, res) => {

    db.all('SELECT * FROM configuracion', [], (err, rows) => {

        if (err) {
            return res.status(500).json({ error: err.message });
        }

        let configObj = {};

        rows.forEach(c => configObj[c.clave] = c.valor);

        res.json({ configuracion: configObj });
    });
});

app.post('/api/configuracion', (req, res) => {

    const { logoAppUri, gamaColor } = req.body;

    db.serialize(() => {

        if (logoAppUri) {
            db.run(
                'INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)',
                ['logoAppUri', logoAppUri]
            );
        }

        if (gamaColor) {
            db.run(
                'INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)',
                ['gamaColor', gamaColor]
            );
        }

        res.json({
            mensaje: 'Configuración de marca guardada en el servidor'
        });
    });
});

// ==========================================
// ASISTENTE DE COMPRAS CON IA
// ==========================================

require('./ia')(app);

// ==========================================
// INICIAR SERVIDOR
// ==========================================

app.listen(PORT, '0.0.0.0', () => {

    console.log('=================================================');
    console.log(' Servidor Backend AppCappuCare ejecutándose');
    console.log(` Puerto: http://localhost:${PORT}`);
    console.log('=================================================');
});
