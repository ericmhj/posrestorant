package com.restaurant.pos.cuenta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class CreatePedidoRequest {

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    public List<ItemRequest> items;

    public static class ItemRequest {
        @NotNull(message = "El productoId es requerido")
        public UUID productoId;

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        public int cantidad;

        public String modificadores;
    }
}
