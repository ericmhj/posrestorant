package com.restaurant.pos.kds;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
import com.restaurant.pos.mesa.Mesa;
import com.restaurant.pos.mesa.MesaEstado;
import com.restaurant.pos.pedido.ItemPedido;
import com.restaurant.pos.pedido.ItemPedidoDTO;
import com.restaurant.pos.pedido.ItemPedidoEstado;
import com.restaurant.pos.pedido.Pedido;
import com.restaurant.pos.producto.Categoria;
import com.restaurant.pos.producto.Estacion;
import com.restaurant.pos.producto.Producto;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class KDSServiceTest {

    @Inject
    KDSService kdsService;

    @Transactional
    ItemPedido createKDSItem(ItemPedidoEstado estado) {
        Mesa mesa = new Mesa();
        mesa.nombre = "KDSTest-" + UUID.randomUUID();
        mesa.estado = MesaEstado.OCUPADA;
        mesa.persist();

        Cuenta cuenta = new Cuenta();
        cuenta.mesa = mesa;
        cuenta.estado = CuentaEstado.ABIERTA;
        cuenta.abiertaEn = LocalDateTime.now();
        cuenta.total = BigDecimal.ZERO;
        cuenta.persist();

        Categoria cat = new Categoria();
        cat.nombre = "KDSCat-" + UUID.randomUUID();
        cat.persist();

        Producto producto = new Producto();
        producto.nombre = "KDSProducto-" + UUID.randomUUID();
        producto.precio = new BigDecimal("50.00");
        producto.estacion = Estacion.COCINA;
        producto.categoria = cat;
        producto.activo = true;
        producto.persist();

        Pedido pedido = new Pedido();
        pedido.cuenta = cuenta;
        pedido.numeroRonda = 1;
        pedido.persist();

        ItemPedido item = new ItemPedido();
        item.pedido = pedido;
        item.producto = producto;
        item.cantidad = 1;
        item.precioUnitario = producto.precio;
        item.estado = estado;
        item.persist();

        return item;
    }

    @Transactional
    void cleanup(UUID itemId) {
        ItemPedido.findByIdOptional(itemId).ifPresent(i -> {
            ItemPedido item = (ItemPedido) i;
            UUID pedidoId = item.pedido.id;
            UUID cuentaId = item.pedido.cuenta.id;
            UUID mesaId = item.pedido.cuenta.mesa.id;
            UUID productoId = item.producto.id;
            UUID catId = item.producto.categoria.id;
            item.delete();
            Pedido.findByIdOptional(pedidoId).ifPresent(p -> ((Pedido) p).delete());
            Cuenta.findByIdOptional(cuentaId).ifPresent(c -> ((Cuenta) c).delete());
            Mesa.findByIdOptional(mesaId).ifPresent(m -> ((Mesa) m).delete());
            Producto.findByIdOptional(productoId).ifPresent(p -> ((Producto) p).delete());
            Categoria.findByIdOptional(catId).ifPresent(c -> ((Categoria) c).delete());
        });
    }

    @Test
    @Transactional
    void updateEstado_PENDIENTE_to_PREPARANDO_succeeds() {
        ItemPedido item = createKDSItem(ItemPedidoEstado.PENDIENTE);
        try {
            ItemPedidoDTO dto = kdsService.updateEstado(item.id, ItemPedidoEstado.PREPARANDO, null);
            assertEquals("PREPARANDO", dto.estado);
            assertNotNull(((ItemPedido) ItemPedido.findById(item.id)).preparandoEn);
        } finally {
            cleanup(item.id);
        }
    }

    @Test
    @Transactional
    void updateEstado_PREPARANDO_to_LISTO_succeeds() {
        ItemPedido item = createKDSItem(ItemPedidoEstado.PREPARANDO);
        item.preparandoEn = LocalDateTime.now();
        try {
            ItemPedidoDTO dto = kdsService.updateEstado(item.id, ItemPedidoEstado.LISTO, null);
            assertEquals("LISTO", dto.estado);
        } finally {
            cleanup(item.id);
        }
    }

    @Test
    @Transactional
    void updateEstado_invalidTransition_throws409() {
        ItemPedido item = createKDSItem(ItemPedidoEstado.PENDIENTE);
        try {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> kdsService.updateEstado(item.id, ItemPedidoEstado.LISTO, null));
            assertEquals(409, ex.getHttpStatus());
        } finally {
            cleanup(item.id);
        }
    }

    @Test
    @Transactional
    void updateEstado_LISTO_to_anything_throws409() {
        ItemPedido item = createKDSItem(ItemPedidoEstado.LISTO);
        try {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> kdsService.updateEstado(item.id, ItemPedidoEstado.PREPARANDO, null));
            assertEquals(409, ex.getHttpStatus());
        } finally {
            cleanup(item.id);
        }
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 13: Items KDS/BDS ordenados por timestamp")
    @Transactional
    void property13_kdsItemsOrderedByCreatedAtAsc() {
        // Create 5 items with different timestamps
        ItemPedido item1 = createKDSItem(ItemPedidoEstado.PENDIENTE);
        item1.createdAt = LocalDateTime.now().minusMinutes(10);
        ItemPedido item2 = createKDSItem(ItemPedidoEstado.PENDIENTE);
        item2.createdAt = LocalDateTime.now().minusMinutes(5);
        ItemPedido item3 = createKDSItem(ItemPedidoEstado.PENDIENTE);
        item3.createdAt = LocalDateTime.now().minusMinutes(1);

        try {
            List<ItemPedidoDTO> items = kdsService.findItems();
            // Verify ordering: each item's tiempoEspera should be >= next item's
            for (int i = 0; i < items.size() - 1; i++) {
                Long t1 = items.get(i).tiempoEspera;
                Long t2 = items.get(i + 1).tiempoEspera;
                if (t1 != null && t2 != null) {
                    assertTrue(t1 >= t2,
                            "Items must be ordered oldest first (tiempoEspera desc)");
                }
            }
        } finally {
            cleanup(item1.id);
            cleanup(item2.id);
            cleanup(item3.id);
        }
    }
}
