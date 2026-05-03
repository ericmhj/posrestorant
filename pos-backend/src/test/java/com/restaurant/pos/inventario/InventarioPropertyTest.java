package com.restaurant.pos.inventario;

import com.restaurant.pos.common.BusinessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Inventario.
 *
 * Property 10: Item rejected when stock < quantity
 * Property 20: Stock never goes negative
 * Property 21: Every stock change creates exactly one MovimientoInventario
 */
@QuarkusTest
class InventarioPropertyTest {

    @Inject
    InventarioService inventarioService;

    @Transactional
    ItemInventario createItem(String nombre, BigDecimal stock) {
        ItemInventario item = new ItemInventario();
        item.nombre = nombre;
        item.stockActual = stock;
        item.stockMinimo = BigDecimal.ZERO;
        item.unidadMedida = "unidad";
        item.persist();
        return item;
    }

    @Transactional
    void cleanup(UUID itemId) {
        ReservaInventario.delete("itemInventario.id", itemId);
        MovimientoInventario.delete("itemInventario.id", itemId);
        ItemInventario.findByIdOptional(itemId).ifPresent(i -> ((ItemInventario) i).delete());
    }

    // -------------------------------------------------------
    // Property 10: Item rejected when stock insufficient
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 10: Item rechazado cuando stock insuficiente")
    @Transactional
    void property10_itemRejectedWhenStockInsufficient() {
        // Test with 20 different stock/quantity combinations
        int[][] cases = {
            {0, 1}, {1, 2}, {5, 6}, {10, 11}, {0, 100},
            {3, 4}, {7, 8}, {2, 3}, {1, 10}, {4, 5},
            {0, 50}, {9, 10}, {6, 7}, {8, 9}, {3, 100},
            {1, 1000}, {2, 5}, {0, 1000}, {5, 10}, {4, 100}
        };

        for (int[] c : cases) {
            ItemInventario item = createItem("PropTest10-" + c[0] + "-" + c[1],
                    new BigDecimal(c[0]));
            BigDecimal stockBefore = item.stockActual;
            try {
                assertThrows(BusinessException.StockInsuficienteException.class,
                        () -> inventarioService.reservar(item.id,
                                new BigDecimal(c[1]), UUID.randomUUID()),
                        "Should reject when stock=" + c[0] + " < cantidad=" + c[1]);

                // Stock must remain unchanged after rejection
                ItemInventario unchanged = (ItemInventario) ItemInventario.findById(item.id);
                assertEquals(stockBefore, unchanged.stockActual,
                        "Stock must not change after rejected reservation");
            } finally {
                cleanup(item.id);
            }
        }
    }

    // -------------------------------------------------------
    // Property 20: Stock never goes negative
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 20: Stock nunca es negativo")
    @Transactional
    void property20_stockNeverGoesNegative() {
        // Test 20 combinations of initial stock and requested deduction
        int[] stocks = {0, 1, 2, 5, 10, 3, 7, 4, 8, 6, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] deductions = {1, 2, 3, 6, 11, 4, 8, 5, 9, 7, 100, 50, 10, 20, 15, 30, 25, 40, 35, 50};

        for (int i = 0; i < stocks.length; i++) {
            if (deductions[i] <= stocks[i]) continue; // only test cases where deduction > stock

            ItemInventario item = createItem("PropTest20-" + i,
                    new BigDecimal(stocks[i]));
            try {
                RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
                req.tipo = TipoMovimiento.MERMA;
                req.cantidad = new BigDecimal(deductions[i]);
                req.motivo = "Test merma";

                assertThrows(BusinessException.class,
                        () -> inventarioService.registrarMovimiento(item.id, req, null));

                // Stock must remain >= 0
                ItemInventario unchanged = (ItemInventario) ItemInventario.findById(item.id);
                assertTrue(unchanged.stockActual.compareTo(BigDecimal.ZERO) >= 0,
                        "Stock must never be negative, was: " + unchanged.stockActual);
            } finally {
                cleanup(item.id);
            }
        }
    }

    // -------------------------------------------------------
    // Property 21: Every stock change creates exactly one MovimientoInventario
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 21: Movimiento de inventario registrado por cada cambio")
    @Transactional
    void property21_everyStockChangeCreatesOneMovimiento() {
        TipoMovimiento[] tipos = {
            TipoMovimiento.ENTRADA, TipoMovimiento.ENTRADA, TipoMovimiento.ENTRADA,
            TipoMovimiento.AJUSTE, TipoMovimiento.AJUSTE, TipoMovimiento.AJUSTE,
            TipoMovimiento.MERMA, TipoMovimiento.MERMA, TipoMovimiento.MERMA,
            TipoMovimiento.ENTRADA
        };
        BigDecimal[] cantidades = {
            new BigDecimal("5"), new BigDecimal("10"), new BigDecimal("1"),
            new BigDecimal("2"), new BigDecimal("3"), new BigDecimal("1"),
            new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("1"),
            new BigDecimal("100")
        };

        for (int i = 0; i < tipos.length; i++) {
            ItemInventario item = createItem("PropTest21-" + i, new BigDecimal("50"));
            long movsBefore = MovimientoInventario.count("itemInventario.id", item.id);
            try {
                RegistrarMovimientoRequest req = new RegistrarMovimientoRequest();
                req.tipo = tipos[i];
                req.cantidad = cantidades[i];
                if (tipos[i] == TipoMovimiento.AJUSTE || tipos[i] == TipoMovimiento.MERMA) {
                    req.motivo = "Test motivo";
                }

                inventarioService.registrarMovimiento(item.id, req, null);

                long movsAfter = MovimientoInventario.count("itemInventario.id", item.id);
                assertEquals(movsBefore + 1, movsAfter,
                        "Exactly one MovimientoInventario must be created per stock change");
            } finally {
                cleanup(item.id);
            }
        }
    }
}
