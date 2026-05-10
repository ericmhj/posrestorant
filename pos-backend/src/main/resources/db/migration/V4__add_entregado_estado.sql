-- V4__add_entregado_estado.sql
-- Agrega el estado ENTREGADO al item_pedido

ALTER TABLE item_pedido DROP CONSTRAINT IF EXISTS item_pedido_estado_check;
ALTER TABLE item_pedido ADD CONSTRAINT item_pedido_estado_check
    CHECK (estado IN ('PENDIENTE','PREPARANDO','LISTO','ENTREGADO'));

ALTER TABLE item_pedido ADD COLUMN IF NOT EXISTS entregado_en TIMESTAMP;
