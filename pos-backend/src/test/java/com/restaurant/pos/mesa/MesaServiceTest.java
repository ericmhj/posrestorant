package com.restaurant.pos.mesa;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MesaServiceTest {

    @Inject
    MesaService mesaService;

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    @Transactional
    Mesa createMesa(String nombre) {
        Mesa m = new Mesa();
        m.nombre = nombre;
        m.estado = MesaEstado.LIBRE;
        m.persist();
        return m;
    }

    @Transactional
    void deleteMesa(UUID id) {
        Mesa.findByIdOptional(id).ifPresent(m -> ((Mesa) m).delete());
    }

    // -------------------------------------------------------
    // Tests
    // -------------------------------------------------------

    @Test
    void findAll_returnsMesas() {
        List<MesaDTO> mesas = mesaService.findAll();
        assertNotNull(mesas);
        // Seed data has 5 mesas
        assertFalse(mesas.isEmpty());
    }

    @Test
    @Transactional
    void create_persistsMesaWithLibreState() {
        MesaDTO dto = mesaService.create(new CreateMesaRequest() {{ nombre = "Mesa Test"; }});
        assertNotNull(dto.id);
        assertEquals("Mesa Test", dto.nombre);
        assertEquals("LIBRE", dto.estado);
        // cleanup
        Mesa.findByIdOptional(dto.id).ifPresent(m -> ((Mesa) m).delete());
    }

    @Test
    @Transactional
    void abrir_mesaLibre_createsCuentaAndSetsOcupada() {
        Mesa mesa = createMesa("Mesa Abrir Test");
        try {
            Cuenta cuenta = mesaService.abrir(mesa.id, null);
            assertNotNull(cuenta.id);
            assertEquals(CuentaEstado.ABIERTA, cuenta.estado);

            Mesa updated = (Mesa) Mesa.findById(mesa.id);
            assertEquals(MesaEstado.OCUPADA, updated.estado);
        } finally {
            Cuenta.findAbiertaByMesaId(mesa.id).ifPresent(c -> c.delete());
            deleteMesa(mesa.id);
        }
    }

    @Test
    @Transactional
    void abrir_mesaOcupada_throws409() {
        Mesa mesa = createMesa("Mesa Ocupada Test");
        mesaService.abrir(mesa.id, null);
        try {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> mesaService.abrir(mesa.id, null));
            assertEquals(409, ex.getHttpStatus());
        } finally {
            Cuenta.findAbiertaByMesaId(mesa.id).ifPresent(c -> c.delete());
            deleteMesa(mesa.id);
        }
    }

    @Test
    @Transactional
    void delete_mesaWithOpenCuenta_throws409() {
        Mesa mesa = createMesa("Mesa Delete Test");
        mesaService.abrir(mesa.id, null);
        try {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> mesaService.delete(mesa.id));
            assertEquals(409, ex.getHttpStatus());
        } finally {
            Cuenta.findAbiertaByMesaId(mesa.id).ifPresent(c -> c.delete());
            deleteMesa(mesa.id);
        }
    }

    @Test
    @Transactional
    void delete_mesaLibre_succeeds() {
        Mesa mesa = createMesa("Mesa Delete Libre");
        assertDoesNotThrow(() -> mesaService.delete(mesa.id));
        assertTrue(Mesa.findByIdOptional(mesa.id).isEmpty());
    }

    @Test
    @Transactional
    void delete_mesaNotFound_throws404() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> mesaService.delete(UUID.randomUUID()));
        assertEquals(404, ex.getHttpStatus());
    }
}
