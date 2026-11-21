
const express = require('express');

// Gestionar las operaciones CRUD de medicamentos
function crearRutasMedicamentos(db) {

    const router = express.Router();

    // Crear tabla de medicamentos
    db.run(`
        CREATE TABLE IF NOT EXISTS medicamentos (
            codigo TEXT PRIMARY KEY,
            nombre TEXT NOT NULL,
            principioActivo TEXT,
            concentracion TEXT,
            laboratorio TEXT,
            stock INTEGER DEFAULT 0
        )
    `, (error) => {
        if (error) {
            console.error('Error al crear tabla:', error.message);
        } else {
            console.log('Tabla medicamentos preparada correctamente');
        }
    });

    // READ - Obtener todos
    router.get('/', (req, res) => {
        db.all('SELECT * FROM medicamentos', [], (error, medicamentos) => {
            if (error) {
                return res.status(500).json({
                    mensaje: 'Error al consultar medicamentos'
                });
            }

            res.json({ medicamentos });
        });
    });

    // READ - Buscar por código QR
    router.get('/codigo/:codigo', (req, res) => {
        const codigo = req.params.codigo.trim();

        db.get(
            'SELECT * FROM medicamentos WHERE codigo = ?',
            [codigo],
            (error, medicamento) => {

                if (error) {
                    return res.status(500).json({
                        mensaje: 'Error al buscar medicamento'
                    });
                }

                if (!medicamento) {
                    return res.status(404).json({
                        mensaje: 'Medicamento no encontrado'
                    });
                }

                res.json({ medicamento });
            }
        );
    });

    // CREATE - Registrar
    router.post('/', (req, res) => {

        const {
            codigo,
            nombre,
            principioActivo,
            concentracion,
            laboratorio,
            stock
        } = req.body;

        if (
            typeof codigo !== 'string' ||
            !codigo.trim() ||
            typeof nombre !== 'string' ||
            !nombre.trim()
        ) {
            return res.status(400).json({
                mensaje: 'El código y el nombre son obligatorios'
            });
        }

        const cantidad = stock === undefined ? 0 : stock;

        if (!Number.isInteger(cantidad) || cantidad < 0) {
            return res.status(400).json({
                mensaje: 'El stock debe ser un número entero positivo o cero'
            });
        }

        const medicamento = {
            codigo: codigo.trim(),
            nombre: nombre.trim(),
            principioActivo: principioActivo || '',
            concentracion: concentracion || '',
            laboratorio: laboratorio || '',
            stock: cantidad
        };

        db.run(`
            INSERT INTO medicamentos
            (codigo, nombre, principioActivo, concentracion, laboratorio, stock)
            VALUES (?, ?, ?, ?, ?, ?)
        `, [
            medicamento.codigo,
            medicamento.nombre,
            medicamento.principioActivo,
            medicamento.concentracion,
            medicamento.laboratorio,
            medicamento.stock
        ], function(error) {

            if (error) {
                if (error.code === 'SQLITE_CONSTRAINT') {
                    return res.status(409).json({
                        mensaje: 'Este código ya está registrado'
                    });
                }

                return res.status(500).json({
                    mensaje: 'Error al registrar medicamento'
                });
            }

            res.status(201).json({
                mensaje: 'Medicamento registrado correctamente',
                medicamento
            });
        });
    });

    // UPDATE - Modificar
    router.put('/:codigo', (req, res) => {

        const codigo = req.params.codigo.trim();

        const {
            nombre,
            principioActivo,
            concentracion,
            laboratorio,
            stock
        } = req.body;

        if (!codigo || typeof nombre !== 'string' || !nombre.trim()) {
            return res.status(400).json({
                mensaje: 'El código y el nombre son obligatorios'
            });
        }

        if (!Number.isInteger(stock) || stock < 0) {
            return res.status(400).json({
                mensaje: 'El stock debe ser un número entero positivo o cero'
            });
        }

        const campos = [principioActivo, concentracion, laboratorio];

        if (campos.some(campo =>
            campo !== undefined && typeof campo !== 'string'
        )) {
            return res.status(400).json({
                mensaje: 'Los datos deben ser texto'
            });
        }

        db.run(`
            UPDATE medicamentos
            SET nombre = ?,
                principioActivo = ?,
                concentracion = ?,
                laboratorio = ?,
                stock = ?
            WHERE codigo = ?
        `, [
            nombre.trim(),
            principioActivo || '',
            concentracion || '',
            laboratorio || '',
            stock,
            codigo
        ], function(error) {

            if (error) {
                return res.status(500).json({
                    mensaje: 'Error al actualizar medicamento'
                });
            }

            if (this.changes === 0) {
                return res.status(404).json({
                    mensaje: 'Medicamento no encontrado'
                });
            }

            res.json({
                mensaje: 'Medicamento actualizado correctamente',
                medicamento: {
                    codigo,
                    nombre: nombre.trim(),
                    principioActivo: principioActivo || '',
                    concentracion: concentracion || '',
                    laboratorio: laboratorio || '',
                    stock
                }
            });
        });
    });

    // DELETE - Eliminar
    router.delete('/:codigo', (req, res) => {

        const codigo = req.params.codigo.trim();

        if (!codigo) {
            return res.status(400).json({
                mensaje: 'El código es obligatorio'
            });
        }

        db.run(
            'DELETE FROM medicamentos WHERE codigo = ?',
            [codigo],
            function(error) {

                if (error) {
                    return res.status(500).json({
                        mensaje: 'Error al eliminar medicamento'
                    });
                }

                if (this.changes === 0) {
                    return res.status(404).json({
                        mensaje: 'Medicamento no encontrado'
                    });
                }

                res.json({
                    mensaje: 'Medicamento eliminado correctamente',
                    codigoEliminado: codigo
                });
            }
        );
    });

    return router;
}

module.exports = crearRutasMedicamentos;
