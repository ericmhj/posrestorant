-- V7__inventario_costo_unitario.sql
-- Agrega costo_unitario a item_inventario para calcular rentabilidad

ALTER TABLE item_inventario
    ADD COLUMN IF NOT EXISTS costo_unitario DECIMAL(10,2) DEFAULT 0;
