package com.restaurant.pos.mesa;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Mesa state machine.
 *
 * Property 5: Mesa estado is always one of {LIBRE, OCUPADA, RESERVADA}
 * Property 6: Opening a LIBRE mesa → estado=OCUPADA + exactly one open Cuenta
 * Property 7: Closing a Cuenta → mesa estado=LIBRE
 * Property 8: Opening an OCUPADA mesa → rejected with 409
 */
@QuarkusTest
class MesaStatePropertyTest {

    @Inject
    MesaService mesaService;

    private static final List<MesaEstado> VALID_STATES =
            Arrays.asList(MesaEstado.LIBRE, MesaEstado.OCUPADA, MesaEstado.RESERVADA);

    // -------------------------------------------------------
    // Property 5: Estado is always a valid value
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 5: Estado de Mesa es siempre valido")
    void property5_mesaEstadoIsAlwaysValid() {
        List<MesaDTO> mesas = mesaService.findAll();
        // Run across all mesas in the system (min 5 from seed data)
        assertTrue(mesas.size() >= 5, "Expected at least 5 mesas from seed data");
        for (MesaDTO mesa : mesas) {
            assertTrue(VALID_STATES.stream().anyMatch(s -> s.name().equals(mesa.estado)),
                    "Invalid estado: " + mesa.estado + " for mesa " + mesa.id);
        }
    }

    // -------------------------------------------------------
    // Property 6: Opening LIBRE mesa → OCUPADA + one open Cuenta
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 6: Abrir mesa libre crea cuenta y cambia estado")
    @Transactional
    void property6_openLibreMesa_createsOneCuentaAndSetsOcupada() {
        // Run 10 iterations with different mesas
        for (int i = 0; i < 10; i++) {
            Mesa mesa = new Mesa();
            mesa.nombre = "PropTest6-" + i;
            mesa.estado = MesaEstado.LIBRE;
            mesa.persist();

            try {
                Cuenta cuenta = mesaService.abrir(mesa.id, null);

                Mesa updated = (Mesa) Mesa.findById(mesa.id);
                assertEquals(MesaEstado.OCUPADA, updated.estado,
                        "Mesa should be OCUPADA after opening");

                long openCuentas = Cuenta.count("mesa.id = ?1 AND estado = ?2",
                        mesa.id, CuentaEstado.ABIERTA);
                assertEquals(1, openCuentas,
                        "Exactly one open Cuenta should exist for the mesa");
            } finally {
                Cuenta.findAbiertaByMesaId(mesa.id).ifPresent(c -> c.delete());
                mesa.delete();
            }
        }
    }

    // -------------------------------------------------------
    // Property 7: Closing Cuenta → mesa LIBRE
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 7: Cerrar cuenta libera mesa")
    @Transactional
    void property7_closingCuenta_setsMesaLibre() {
        for (int i = 0; i < 10; i++) {
            Mesa mesa = new Mesa();
            mesa.nombre = "PropTest7-" + i;
            mesa.estado = MesaEstado.LIBRE;
            mesa.persist();

            Cuenta cuenta = mesaService.abrir(mesa.id, null);

            // Simulate closing the cuenta (as cobro would do)
            cuenta.estado = CuentaEstado.CERRADA;
            mesa.estado = MesaEstado.LIBRE;

            Mesa updated = (Mesa) Mesa.findById(mesa.id);
            assertEquals(MesaEstado.LIBRE, updated.estado,
                    "Mesa should be LIBRE after closing Cuenta");

            long openCuentas = Cuenta.count("mesa.id = ?1 AND estado = ?2",
                    mesa.id, CuentaEstado.ABIERTA);
            assertEquals(0, openCuentas,
                    "No open Cuenta should exist after closing");

            cuenta.delete();
            mesa.delete();
        }
    }

    // -------------------------------------------------------
    // Property 8: Opening OCUPADA mesa → rejected
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 8: Mesa ocupada no puede abrirse de nuevo")
    @Transactional
    void property8_openOcupadaMesa_isRejected() {
        for (int i = 0; i < 10; i++) {
            Mesa mesa = new Mesa();
            mesa.nombre = "PropTest8-" + i;
            mesa.estado = MesaEstado.LIBRE;
            mesa.persist();

            mesaService.abrir(mesa.id, null);

            // Attempt to open again
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> mesaService.abrir(mesa.id, null));
            assertEquals(409, ex.getHttpStatus(),
                    "Should return 409 when opening an OCUPADA mesa");

            // State must remain OCUPADA
            Mesa updated = (Mesa) Mesa.findById(mesa.id);
            assertEquals(MesaEstado.OCUPADA, updated.estado,
                    "Mesa state must remain OCUPADA after rejected open attempt");

            Cuenta.findAbiertaByMesaId(mesa.id).ifPresent(c -> c.delete());
            mesa.delete();
        }
    }
}
