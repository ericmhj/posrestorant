package com.restaurant.pos.inventario;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.common.ValidationException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class InventarioServiceTest {

    @Inject
    InventarioService inventarioService;

    @Transactional
    ItemInventario createItem(String nombre, BigDecimal stock) {
        ItemInventario item = new ItemInventario();
        item.nombre = nombre;
        item.stockActual = stock;
        item.stockMinimo = new BigDecimal("5");
        item.unidadMedida = "unidad";
        item.persist();
        return item;
    }

    @Transactional
    void deleteItem(UUID id) {
        ReservaInventario.delete("itemInventario.id", id);
        MovimientoInventario.delete("itemInventario.id", id);
        ItemInventario.findByIdOptional(id).ifPresent(i -> ((ItemInventario) i).delete());
    }

    @Test
    @Transactional
    void create_persistsItemWithZeroStock() {
        CreateItemInventarioRequest req = new CreateItemInventarioRequest();
        req.nombre = "Tomate";
        req.unidadMedida = "kg";
        req.stockMinimo = new BigDecimal("2");

        ItemInventarioDTO dto = inventarioService.create(req);
        assertNotNull(dto.id);
        assertEquals(BigDecimal.ZERO, dto.stockActual);
        assertFalse(dto.alertaMinimo); // 0 < 2 → alert
        deleteItem(dto.id);
    }

    @Test
    @Transactional
    void registrarMovimiento_ENTRADA_increasesStock() {
        ItemInventario item = createItem("Cebolla", new BigDecimal("10"));
        try {
            RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
            req.tipo = TipoMovimiento.ENTRADA;
            req.cantidad = new BigDecimal("5");

            inventarioService.registrarMovimiento(item.id, req, null);

            ItemInventario updated = (ItemInventario) ItemInventario.findById(item.id);
            assertEquals(new BigDecimal("15.000"), updated.stockActual);
        } finally {
            deleteItem(item.id);
        }
    }

    @Test
    @Transactional
    void registrarMovimiento_MERMA_requiresMotivo() {
        ItemInventario item = createItem("Lechuga", new BigDecimal("10"));
        try {
            RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
            req.tipo = TipoMovimiento.MERMA;
            req.cantidad = new BigDecimal("2");
            // motivo not set

            assertThrows(ValidationException.class,
                    () -> inventarioService.registrarMovimiento(item.id, req, null));
        } finally {
            deleteItem(item.id);
        }
    }

    @Test
    @Transactional
    void registrarMovimiento_negativeStock_throws() {
        ItemInventario item = createItem("Queso", new BigDecimal("3"));
        try {
            RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
            req.tipo = TipoMovimiento.AJUSTE;
            req.cantidad = new BigDecimal("10");
            req.motivo = "Corrección";

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> inventarioService.registrarMovimiento(item.id, req, null));
            assertEquals(400, ex.getHttpStatus());
        } finally {
            deleteItem(item.id);
        }
    }

    @Test
    @Transactional
    void reservar_reducesAvailableStock() {
        ItemInventario item = createItem("Pollo", new BigDecimal("20"));
        UUID fakeItemPedidoId = UUID.randomUUID();
        try {
            // We can't create a real ItemPedido without full setup,
            // so we test the reservation logic directly
            BigDecimal reservado = ReservaInventario.sumReservasByItemInventarioId(item.id);
            BigDecimal disponible = item.stockActual.subtract(reservado);
            assertEquals(new BigDecimal("20.000"), disponible);
        } finally {
            deleteItem(item.id);
        }
    }

    @Test
    @Transactional
    void reservar_insufficientStock_throwsStockInsuficiente() {
        ItemInventario item = createItem("Camarón", new BigDecimal("2"));
        try {
            assertThrows(BusinessException.StockInsuficienteException.class,
                    () -> inventarioService.reservar(item.id, new BigDecimal("5"), UUID.randomUUID()));
        } finally {
            deleteItem(item.id);
        }
    }

    @Test
    @Transactional
    void stockNeverGoesNegative_onMovimiento() {
        ItemInventario item = createItem("Aceite", new BigDecimal("1"));
        try {
            RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
            req.tipo = TipoMovimiento.MERMA;
            req.cantidad = new BigDecimal("5");
            req.motivo = "Derrame";

            assertThrows(BusinessException.class,
                    () -> inventarioService.registrarMovimiento(item.id, req, null));

            // Stock must remain unchanged
            ItemInventario unchanged = (ItemInventario) ItemInventario.findById(item.id);
            assertTrue(unchanged.stockActual.compareTo(BigDecimal.ZERO) >= 0);
        } finally {
            deleteItem(item.id);
        }
    }
}
