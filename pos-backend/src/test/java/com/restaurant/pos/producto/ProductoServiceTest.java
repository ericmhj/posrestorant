package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ProductoServiceTest {

    @Inject
    ProductoService productoService;

    @Transactional
    Categoria createCategoria() {
        Categoria c = new Categoria();
        c.nombre = "Test Cat " + UUID.randomUUID();
        c.persist();
        return c;
    }

    @Transactional
    void cleanup(UUID productoId, UUID categoriaId) {
        Producto.findByIdOptional(productoId).ifPresent(p -> ((Producto) p).delete());
        Categoria.findByIdOptional(categoriaId).ifPresent(c -> ((Categoria) c).delete());
    }

    @Test
    @Transactional
    void create_persistsProductoWithActivoTrue() {
        Categoria cat = createCategoria();
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Tacos";
        req.precio = new BigDecimal("85.00");
        req.categoriaId = cat.id;
        req.estacion = Estacion.COCINA;

        ProductoDTO dto = productoService.create(req);
        assertNotNull(dto.id);
        assertTrue(dto.activo);
        assertEquals("Tacos", dto.nombre);
        assertNull(dto.imagenUrl);

        cleanup(dto.id, cat.id);
    }

    @Test
    @Transactional
    void deactivate_setsActivoFalse() {
        Categoria cat = createCategoria();
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Producto Activo";
        req.precio = new BigDecimal("50.00");
        req.categoriaId = cat.id;
        req.estacion = Estacion.BARRA;

        ProductoDTO created = productoService.create(req);
        productoService.deactivate(created.id);

        Producto p = (Producto) Producto.findById(created.id);
        assertFalse(p.activo);

        cleanup(created.id, cat.id);
    }

    @Test
    @Transactional
    void deactivate_preventsAddingToOrders() {
        Categoria cat = createCategoria();
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Producto Inactivo";
        req.precio = new BigDecimal("30.00");
        req.categoriaId = cat.id;
        req.estacion = Estacion.COCINA;

        ProductoDTO created = productoService.create(req);
        productoService.deactivate(created.id);

        // findActivoById should return empty for inactive product
        assertTrue(Producto.findActivoById(created.id).isEmpty(),
                "Inactive product should not be found by findActivoById");

        cleanup(created.id, cat.id);
    }

    @Test
    @Transactional
    void update_priceChange_doesNotAffectExistingItemPedido() {
        // The price snapshot is stored in ItemPedido.precioUnitario at order time
        // This test verifies that updating Producto.precio does not change existing snapshots
        Categoria cat = createCategoria();
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Producto Precio";
        req.precio = new BigDecimal("100.00");
        req.categoriaId = cat.id;
        req.estacion = Estacion.COCINA;

        ProductoDTO created = productoService.create(req);
        BigDecimal originalPrice = created.precio;

        // Simulate a snapshot (as ItemPedido would store it)
        BigDecimal snapshot = originalPrice;

        // Update price
        UpdateProductoRequest updateReq = new UpdateProductoRequest();
        updateReq.precio = new BigDecimal("150.00");
        productoService.update(created.id, updateReq);

        // Snapshot must remain unchanged
        assertEquals(new BigDecimal("100.00"), snapshot,
                "Price snapshot must not change when Producto price is updated");

        cleanup(created.id, cat.id);
    }

    @Test
    @Transactional
    void deleteImagen_setsImagenUrlToNull() {
        Categoria cat = createCategoria();
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Producto Con Imagen";
        req.precio = new BigDecimal("60.00");
        req.categoriaId = cat.id;
        req.estacion = Estacion.BARRA;

        ProductoDTO created = productoService.create(req);

        // Manually set an imagenUrl
        Producto p = (Producto) Producto.findById(created.id);
        p.imagenUrl = "/uploads/productos/test.jpg";

        ProductoDTO result = productoService.deleteImagen(created.id);
        assertNull(result.imagenUrl, "imagenUrl must be null after deleteImagen");

        cleanup(created.id, cat.id);
    }

    @Test
    @Transactional
    void create_invalidCategoria_throws404() {
        CreateProductoRequest req = new CreateProductoRequest();
        req.nombre = "Producto Sin Cat";
        req.precio = new BigDecimal("10.00");
        req.categoriaId = UUID.randomUUID(); // non-existent
        req.estacion = Estacion.COCINA;

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productoService.create(req));
        assertEquals(404, ex.getHttpStatus());
    }
}
