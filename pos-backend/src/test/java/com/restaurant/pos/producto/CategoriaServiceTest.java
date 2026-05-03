package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CategoriaServiceTest {

    @Inject
    CategoriaService categoriaService;

    @Transactional
    Categoria createCategoria(String nombre) {
        Categoria c = new Categoria();
        c.nombre = nombre;
        c.persist();
        return c;
    }

    @Transactional
    void deleteCategoria(UUID id) {
        Categoria.findByIdOptional(id).ifPresent(c -> ((Categoria) c).delete());
    }

    @Test
    void findAll_returnsCategoriasFromSeed() {
        List<CategoriaDTO> cats = categoriaService.findAll();
        assertFalse(cats.isEmpty());
        // Seed data has 5 categories
        assertTrue(cats.size() >= 5);
    }

    @Test
    @Transactional
    void create_persistsCategoria() {
        CategoriaDTO dto = categoriaService.create(
                new CreateCategoriaRequest() {{ nombre = "Test Cat"; descripcion = "Desc"; }});
        assertNotNull(dto.id);
        assertEquals("Test Cat", dto.nombre);
        deleteCategoria(dto.id);
    }

    @Test
    @Transactional
    void delete_categoriaWithNoProducts_succeeds() {
        Categoria cat = createCategoria("Empty Cat");
        assertDoesNotThrow(() -> categoriaService.delete(cat.id));
        assertTrue(Categoria.findByIdOptional(cat.id).isEmpty());
    }

    @Test
    @Transactional
    void delete_categoriaWithProducts_throws409WithCount() {
        // Use an existing seed category that has products
        // Seed has 5 categories; we create a product for a new one
        Categoria cat = createCategoria("Cat With Product");
        Producto p = new Producto();
        p.nombre = "Test Producto";
        p.precio = new java.math.BigDecimal("10.00");
        p.estacion = Estacion.COCINA;
        p.categoria = cat;
        p.activo = true;
        p.persist();

        try {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> categoriaService.delete(cat.id));
            assertEquals(409, ex.getHttpStatus());
            assertTrue(ex.getMessage().contains("1"));
        } finally {
            p.delete();
            deleteCategoria(cat.id);
        }
    }

    @Test
    @Transactional
    void delete_notFound_throws404() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoriaService.delete(UUID.randomUUID()));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    @Transactional
    void update_changesNombre() {
        Categoria cat = createCategoria("Old Name");
        try {
            CategoriaDTO dto = categoriaService.update(cat.id,
                    new CreateCategoriaRequest() {{ nombre = "New Name"; }});
            assertEquals("New Name", dto.nombre);
        } finally {
            deleteCategoria(cat.id);
        }
    }
}
