-- V5__inventario_categoria.sql
-- Agrega categoria_id a item_inventario (reutiliza tabla categoria de productos)

ALTER TABLE item_inventario
    ADD COLUMN IF NOT EXISTS categoria_id UUID REFERENCES categoria(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_item_inventario_categoria ON item_inventario(categoria_id);
