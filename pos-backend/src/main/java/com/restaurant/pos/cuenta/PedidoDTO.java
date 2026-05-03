package com.restaurant.pos.cuenta;

import com.restaurant.pos.pedido.ItemPedidoDTO;
import com.restaurant.pos.pedido.Pedido;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PedidoDTO {

    public UUID id;
    public Integer numeroRonda;
    public List<ItemPedidoDTO> items;
    public LocalDateTime createdAt;

    public static PedidoDTO from(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.id = p.id;
        dto.numeroRonda = p.numeroRonda;
        dto.createdAt = p.createdAt;
        dto.items = p.items != null
                ? p.items.stream().map(ItemPedidoDTO::from).collect(Collectors.toList())
                : List.of();
        return dto;
    }
}
