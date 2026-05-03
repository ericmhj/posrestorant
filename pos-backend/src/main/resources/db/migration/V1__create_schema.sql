-- V1__create_schema.sql
-- POS Restaurant System - Initial Schema
-- Requires PostgreSQL 13+ (gen_random_uuid() built-in)

-- ============================================================
-- TABLE: usuario
-- ============================================================
CREATE TABLE usuario (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    username         VARCHAR(50)  UNIQUE NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    rol              VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN','MESERO','COCINA','BARRA')),
    activo           BOOLEAN      DEFAULT true,
    intentos_fallidos INT         DEFAULT 0,
    bloqueado_hasta  TIMESTAMP,
    created_at       TIMESTAMP    DEFAULT NOW()
);

-- ============================================================
-- TABLE: auditoria_acceso
-- ============================================================
CREATE TABLE auditoria_acceso (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID        REFERENCES usuario(id),
    accion      VARCHAR(50),
    resultado   VARCHAR(20),
    fecha_hora  TIMESTAMP   DEFAULT NOW(),
    ip_address  VARCHAR(45)
);

-- ============================================================
-- TABLE: mesa
-- ============================================================
CREATE TABLE mesa (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre     VARCHAR(50) NOT NULL,
    estado     VARCHAR(20) NOT NULL DEFAULT 'LIBRE' CHECK (estado IN ('LIBRE','OCUPADA','RESERVADA')),
    created_at TIMESTAMP   DEFAULT NOW(),
    updated_at TIMESTAMP   DEFAULT NOW()
);

-- ============================================================
-- TABLE: cuenta
-- ============================================================
CREATE TABLE cuenta (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    mesa_id      UUID           REFERENCES mesa(id),
    mesero_id    UUID           REFERENCES usuario(id),
    estado       VARCHAR(20)    NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA','CERRADA')),
    abierta_en   TIMESTAMP      DEFAULT NOW(),
    cerrada_en   TIMESTAMP,
    metodo_pago  VARCHAR(30),
    total        DECIMAL(10,2)
);

-- ============================================================
-- TABLE: categoria
-- ============================================================
CREATE TABLE categoria (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- ============================================================
-- TABLE: producto
-- ============================================================
CREATE TABLE producto (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_id UUID           REFERENCES categoria(id),
    nombre       VARCHAR(150)   NOT NULL,
    descripcion  TEXT,
    precio       DECIMAL(10,2)  NOT NULL CHECK (precio > 0),
    estacion     VARCHAR(20)    NOT NULL CHECK (estacion IN ('COCINA','BARRA')),
    activo       BOOLEAN        DEFAULT true,
    imagen_url   VARCHAR(500),
    created_at   TIMESTAMP      DEFAULT NOW(),
    updated_at   TIMESTAMP      DEFAULT NOW()
);

-- ============================================================
-- TABLE: pedido
-- ============================================================
CREATE TABLE pedido (
    id            UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id     UUID    REFERENCES cuenta(id),
    mesero_id     UUID    REFERENCES usuario(id),
    numero_ronda  INT     NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLE: item_pedido
-- ============================================================
CREATE TABLE item_pedido (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id        UUID           REFERENCES pedido(id),
    producto_id      UUID           REFERENCES producto(id),
    cantidad         INT            NOT NULL CHECK (cantidad > 0),
    precio_unitario  DECIMAL(10,2)  NOT NULL,
    modificadores    TEXT,
    estado           VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','PREPARANDO','LISTO')),
    created_at       TIMESTAMP      DEFAULT NOW(),
    preparando_en    TIMESTAMP,
    listo_en         TIMESTAMP
);

-- ============================================================
-- TABLE: item_inventario
-- ============================================================
CREATE TABLE item_inventario (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(150)   NOT NULL,
    unidad_medida   VARCHAR(30),
    stock_actual    DECIMAL(10,3)  NOT NULL DEFAULT 0,
    stock_minimo    DECIMAL(10,3)  NOT NULL DEFAULT 0,
    stock_maximo    DECIMAL(10,3),
    updated_at      TIMESTAMP      DEFAULT NOW()
);

-- ============================================================
-- TABLE: movimiento_inventario
-- ============================================================
CREATE TABLE movimiento_inventario (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    item_inventario_id  UUID           REFERENCES item_inventario(id),
    usuario_id          UUID           REFERENCES usuario(id),
    tipo                VARCHAR(30)    NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA_VENTA','AJUSTE','MERMA')),
    cantidad            DECIMAL(10,3)  NOT NULL,
    motivo              TEXT,
    proveedor           VARCHAR(150),
    fecha_hora          TIMESTAMP      DEFAULT NOW()
);

-- ============================================================
-- TABLE: reserva_inventario
-- ============================================================
CREATE TABLE reserva_inventario (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    item_inventario_id  UUID           REFERENCES item_inventario(id),
    item_pedido_id      UUID           REFERENCES item_pedido(id),
    cantidad            DECIMAL(10,3)  NOT NULL,
    created_at          TIMESTAMP      DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_usuario_username         ON usuario(username);
CREATE INDEX idx_mesa_estado              ON mesa(estado);
CREATE INDEX idx_cuenta_mesa_estado       ON cuenta(mesa_id, estado);
CREATE INDEX idx_item_pedido_pedido_estado ON item_pedido(pedido_id, estado);
CREATE INDEX idx_item_pedido_estado_fecha  ON item_pedido(estado, created_at);
