package com.restaurant.pos.cuenta;

import com.restaurant.pos.pedido.ItemPedidoDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ComprobanteDTO {

    public UUID numeroCuenta;
    public String mesa;
    public List<ItemPedidoDTO> items;
    public BigDecimal subtotal;
    public BigDecimal impuestos;
    public BigDecimal total;
    public String metodoPago;
    public BigDecimal cambio;
    public LocalDateTime cerradaEn;
}
