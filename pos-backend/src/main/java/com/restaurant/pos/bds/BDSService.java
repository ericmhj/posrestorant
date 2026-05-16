package com.restaurant.pos.bds;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.pedido.ItemPedido;
import com.restaurant.pos.pedido.ItemPedidoDTO;
import com.restaurant.pos.pedido.ItemPedidoEstado;
import com.restaurant.pos.producto.Estacion;
import com.restaurant.pos.websocket.WebSocketBroadcastService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BDSService {

    private static final Logger LOG = Logger.getLogger(BDSService.class);

    @Inject
    WebSocketBroadcastService broadcastService;

    public List<ItemPedidoDTO> findItems() {
        return ItemPedido.findByEstacionAndEstados(
                Estacion.BARRA,
                List.of(ItemPedidoEstado.PENDIENTE, ItemPedidoEstado.PREPARANDO)
        ).stream().map(ItemPedidoDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public ItemPedidoDTO updateEstado(UUID itemId, ItemPedidoEstado nuevoEstado, UUID userId) {
        ItemPedido item = ItemPedido.findByIdOptional(itemId)
                .map(i -> (ItemPedido) i)
                .orElseThrow(() -> new BusinessException(404, "Item no encontrado: " + itemId));

        validateTransition(item.estado, nuevoEstado);

        item.estado = nuevoEstado;
        if (nuevoEstado == ItemPedidoEstado.PREPARANDO) {
            item.preparandoEn = LocalDateTime.now();
        } else if (nuevoEstado == ItemPedidoEstado.LISTO) {
            item.listoEn = LocalDateTime.now();
        }

        broadcastService.broadcastItemEstadoCambiado(Map.of(
                "itemPedidoId", itemId,
                "cuentaId", item.pedido.cuenta.id,
                "mesaId", item.pedido.cuenta.mesa.id,
                "nuevoEstado", nuevoEstado.name(),
                "estacion", "BARRA"
        ));

        checkPedidoCompleto(item);

        LOG.infof("BDS item estado updated: itemId=%s estado=%s", itemId, nuevoEstado);
        return ItemPedidoDTO.from(item);
    }

    private void validateTransition(ItemPedidoEstado current, ItemPedidoEstado next) {
        boolean valid = switch (current) {
            case PENDIENTE -> next == ItemPedidoEstado.PREPARANDO;
            case PREPARANDO -> next == ItemPedidoEstado.LISTO;
            case LISTO -> next == ItemPedidoEstado.ENTREGADO;
            case ENTREGADO -> false;
        };
        if (!valid) {
            throw new BusinessException(409,
                    "Transición de estado inválida: " + current + " → " + next);
        }
    }

    private void checkPedidoCompleto(ItemPedido item) {
        long pendientes = ItemPedido.count(
                "pedido.id = ?1 AND estado != ?2",
                item.pedido.id, ItemPedidoEstado.LISTO);
        if (pendientes == 0) {
            broadcastService.broadcastPedidoCompleto(Map.of(
                    "pedidoId", item.pedido.id,
                    "cuentaId", item.pedido.cuenta.id,
                    "mesaNombre", item.pedido.cuenta.mesa.nombre
            ));
        }
    }
}
