package com.restaurant.pos.producto;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property 18: Inactive product cannot be added to orders.
 *
 * For any Producto with activo=false, findActivoById must return empty,
 * preventing it from being added to any Pedido.
 */
@QuarkusTest
class ProductoDesactivadoPropertyTest {

    @Inject
    ProductoService productoService;

    @Transactional
    Categoria createCategoria() {
        Categoria c = new Categoria();
        c.nombre = "DesactivadoTest-" + UUID.randomUUID();
        c.persist();
        return c;
    }

    @Transactional
    void cleanup(UUID productoId, UUID categoriaId) {
        Producto.findByIdOptional(productoId).ifPresent(p -> ((Producto) p).delete());
        Categoria.findByIdOptional(categoriaId).ifPresent(c -> ((Categoria) c).delete());
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 18: Producto desactivado no puede agregarse a pedidos")
    @Transactional
    void property18_inactiveProductCannotBeAddedToOrders() {
        // Test with 20 different products
        for (int i = 0; i < 20; i++) {
            Categoria cat = createCategoria();
            CreateProductoRequest req = new CreateProductoRequest();
            req.nombre = "PropTest18-" + i;
            req.precio = new BigDecimal("10.00").add(new BigDecimal(i));
            req.categoriaId = cat.id;
            req.estacion = i % 2 == 0 ? Estacion.COCINA : Estacion.BARRA;

            ProductoDTO created = productoService.create(req);

            // Deactivate the product
            productoService.deactivate(created.id);

            // findActivoById must return empty — this is what CuentaService uses
            // to validate before adding to a Pedido
            assertTrue(Producto.findActivoById(created.id).isEmpty(),
                    "Inactive product id=" + created.id +
                    " must not be found by findActivoById (used by order validation)");

            cleanup(created.id, cat.id);
        }
    }
}
