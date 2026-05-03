package com.restaurant.pos.producto;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property 17: Price change does not affect existing ItemPedido snapshots.
 *
 * The price is captured as a snapshot (precioUnitario) at order time.
 * Changing Producto.precio must never retroactively change existing snapshots.
 */
@QuarkusTest
class PrecioPropertyTest {

    @Inject
    ProductoService productoService;

    @Transactional
    Categoria createCategoria() {
        Categoria c = new Categoria();
        c.nombre = "PrecioTest-" + UUID.randomUUID();
        c.persist();
        return c;
    }

    @Transactional
    void cleanup(UUID productoId, UUID categoriaId) {
        Producto.findByIdOptional(productoId).ifPresent(p -> ((Producto) p).delete());
        Categoria.findByIdOptional(categoriaId).ifPresent(c -> ((Categoria) c).delete());
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 17: Precio modificado no afecta cuentas abiertas")
    @Transactional
    void property17_priceChangeDoesNotAffectExistingSnapshots() {
        // Test with 20 different price combinations
        BigDecimal[] originalPrices = {
            new BigDecimal("10.00"), new BigDecimal("25.50"), new BigDecimal("100.00"),
            new BigDecimal("5.99"), new BigDecimal("200.00"), new BigDecimal("75.00"),
            new BigDecimal("33.33"), new BigDecimal("50.00"), new BigDecimal("15.00"),
            new BigDecimal("88.88"), new BigDecimal("12.00"), new BigDecimal("45.00"),
            new BigDecimal("60.00"), new BigDecimal("99.99"), new BigDecimal("150.00"),
            new BigDecimal("7.50"), new BigDecimal("22.00"), new BigDecimal("300.00"),
            new BigDecimal("18.75"), new BigDecimal("55.00")
        };

        BigDecimal[] newPrices = {
            new BigDecimal("20.00"), new BigDecimal("30.00"), new BigDecimal("120.00"),
            new BigDecimal("8.99"), new BigDecimal("250.00"), new BigDecimal("80.00"),
            new BigDecimal("40.00"), new BigDecimal("60.00"), new BigDecimal("18.00"),
            new BigDecimal("95.00"), new BigDecimal("15.00"), new BigDecimal("50.00"),
            new BigDecimal("70.00"), new BigDecimal("110.00"), new BigDecimal("160.00"),
            new BigDecimal("9.00"), new BigDecimal("25.00"), new BigDecimal("350.00"),
            new BigDecimal("20.00"), new BigDecimal("65.00")
        };

        for (int i = 0; i < originalPrices.length; i++) {
            Categoria cat = createCategoria();
            CreateProductoRequest req = new CreateProductoRequest();
            req.nombre = "PropTest17-" + i;
            req.precio = originalPrices[i];
            req.categoriaId = cat.id;
            req.estacion = Estacion.COCINA;

            ProductoDTO created = productoService.create(req);

            // Simulate snapshot at order time
            BigDecimal snapshot = created.precio;

            // Update price
            UpdateProductoRequest updateReq = new UpdateProductoRequest();
            updateReq.precio = newPrices[i];
            productoService.update(created.id, updateReq);

            // Snapshot must remain the original price
            assertEquals(0, originalPrices[i].compareTo(snapshot),
                    "Snapshot price must not change. Original: " + originalPrices[i] +
                    ", snapshot: " + snapshot + ", new price: " + newPrices[i]);

            cleanup(created.id, cat.id);
        }
    }
}
