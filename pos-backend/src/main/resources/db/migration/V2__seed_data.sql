-- V2__seed_data.sql
-- POS Restaurant System - Seed Data

-- ============================================================
-- ADMIN USER
-- username: admin
-- password: admin123  (bcrypt cost 12)
-- ============================================================
INSERT INTO usuario (id, nombre, apellido, username, password_hash, rol, activo, intentos_fallidos)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'Sistema',
    'admin',
    '$2a$12$sJGdtHSt3uzfZ0TRv/jPXO/wOY6DxK2zqN35vnR9zHETilDZZLzXy',
    'ADMIN',
    true,
    0
);

-- ============================================================
-- CATEGORIAS
-- ============================================================
INSERT INTO categoria (id, nombre, descripcion) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Entradas',               'Platillos para comenzar la experiencia'),
    ('10000000-0000-0000-0000-000000000002', 'Platos Fuertes',         'Platillos principales del menú'),
    ('10000000-0000-0000-0000-000000000003', 'Postres',                'Dulces y postres de temporada'),
    ('10000000-0000-0000-0000-000000000004', 'Bebidas Alcohólicas',    'Cervezas, vinos, cocteles y destilados'),
    ('10000000-0000-0000-0000-000000000005', 'Bebidas Sin Alcohol',    'Aguas, refrescos, jugos y café');

-- ============================================================
-- MESAS
-- ============================================================
INSERT INTO mesa (id, nombre, estado) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Mesa 1', 'LIBRE'),
    ('20000000-0000-0000-0000-000000000002', 'Mesa 2', 'LIBRE'),
    ('20000000-0000-0000-0000-000000000003', 'Mesa 3', 'LIBRE'),
    ('20000000-0000-0000-0000-000000000004', 'Mesa 4', 'LIBRE'),
    ('20000000-0000-0000-0000-000000000005', 'Mesa 5', 'LIBRE');
