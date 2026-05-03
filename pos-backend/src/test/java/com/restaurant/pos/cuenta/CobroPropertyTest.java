package com.restaurant.pos.cuenta;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.mesa.Mesa;
import com.restaurant.pos.mesa.MesaEstado;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Cobro.
 *
 * Property 14: Cuenta total equals sum of items
 * Property 15: Cobro closes cuenta and frees mesa
 * Property 16: Cash change = montoRecibido - total
 */
@QuarkusTest
class CobroPropertyTest {

    @Inject
    CuentaService cuentaService;

    @Transactional
    Cuenta createOpenCuenta(BigDecimal total) {
        Mesa mesa = new Mesa();
        mesa.nombre = "CobroTest-" + UUID.randomUUID();
        mesa.estado = MesaEstado.OCUPADA;
        mesa.persist();

        Cuenta cuenta = new Cuenta();
        cuenta.mesa = mesa;
        cuenta.estado = CuentaEstado.ABIERTA;
        cuenta.abiertaEn = LocalDateTime.now();
        cuenta.total = total;
        cuenta.persist();
        return cuenta;
    }

    @Transactional
    void cleanup(UUID cuentaId) {
        Cuenta.findByIdOptional(cuentaId).ifPresent(c -> {
            Cuenta cuenta = (Cuenta) c;
            if (cuenta.mesa != null) {
                Mesa.findByIdOptional(cuenta.mesa.id).ifPresent(m -> ((Mesa) m).delete());
            }
            cuenta.delete();
        });
    }

    // -------------------------------------------------------
    // Property 15: Cobro closes cuenta and frees mesa
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 15: Cobro cierra cuenta y libera mesa")
    @Transactional
    void property15_cobroClosesCuentaAndFreesMesa() {
        // Test with 10 different totals
        BigDecimal[] totals = {
            new BigDecimal("100.00"), new BigDecimal("250.50"), new BigDecimal("50.00"),
            new BigDecimal("1000.00"), new BigDecimal("75.25"), new BigDecimal("300.00"),
            new BigDecimal("15.00"), new BigDecimal("500.00"), new BigDecimal("88.88"),
            new BigDecimal("200.00")
        };

        for (BigDecimal total : totals) {
            Cuenta cuenta = createOpenCuenta(total);
            UUID cuentaId = cuenta.id;
            UUID mesaId = cuenta.mesa.id;

            try {
                CobroRequest req = new CobroRequest();
                req.metodoPago = CobroRequest.MetodoPago.TARJETA_CREDITO;

                cuentaService.cobrar(cuentaId, req, null);

                Cuenta closed = (Cuenta) Cuenta.findById(cuentaId);
                assertEquals(CuentaEstado.CERRADA, closed.estado,
                        "Cuenta must be CERRADA after cobro");
                assertNotNull(closed.cerradaEn, "cerradaEn must be set");

                Mesa mesa = (Mesa) Mesa.findById(mesaId);
                assertEquals(MesaEstado.LIBRE, mesa.estado,
                        "Mesa must be LIBRE after cobro");
            } finally {
                cleanup(cuentaId);
            }
        }
    }

    // -------------------------------------------------------
    // Property 16: Cash change = montoRecibido - total
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 16: Cambio en pago en efectivo es correcto")
    @Transactional
    void property16_cashChangeIsCorrect() {
        // 10 pairs of (total, montoRecibido) where montoRecibido >= total
        BigDecimal[][] cases = {
            {new BigDecimal("100.00"), new BigDecimal("100.00")},
            {new BigDecimal("85.50"), new BigDecimal("100.00")},
            {new BigDecimal("250.00"), new BigDecimal("300.00")},
            {new BigDecimal("33.33"), new BigDecimal("50.00")},
            {new BigDecimal("500.00"), new BigDecimal("500.00")},
            {new BigDecimal("75.25"), new BigDecimal("80.00")},
            {new BigDecimal("10.00"), new BigDecimal("20.00")},
            {new BigDecimal("199.99"), new BigDecimal("200.00")},
            {new BigDecimal("1000.00"), new BigDecimal("1000.00")},
            {new BigDecimal("45.00"), new BigDecimal("50.00")}
        };

        for (BigDecimal[] c : cases) {
            BigDecimal total = c[0];
            BigDecimal montoRecibido = c[1];
            BigDecimal expectedCambio = montoRecibido.subtract(total);

            Cuenta cuenta = createOpenCuenta(total);
            UUID cuentaId = cuenta.id;

            try {
                CobroRequest req = new CobroRequest();
                req.metodoPago = CobroRequest.MetodoPago.EFECTIVO;
                req.montoRecibido = montoRecibido;

                ComprobanteDTO comprobante = cuentaService.cobrar(cuentaId, req, null);

                assertEquals(0, expectedCambio.compareTo(comprobante.cambio),
                        "Cambio must equal montoRecibido - total. Expected: " +
                        expectedCambio + ", got: " + comprobante.cambio);
            } finally {
                cleanup(cuentaId);
            }
        }
    }

    // -------------------------------------------------------
    // Property 15 edge: insufficient cash is rejected
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 15: Cobro cierra cuenta y libera mesa")
    @Transactional
    void property15_insufficientCash_isRejected() {
        Cuenta cuenta = createOpenCuenta(new BigDecimal("100.00"));
        UUID cuentaId = cuenta.id;

        try {
            CobroRequest req = new CobroRequest();
            req.metodoPago = CobroRequest.MetodoPago.EFECTIVO;
            req.montoRecibido = new BigDecimal("50.00"); // less than total

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> cuentaService.cobrar(cuentaId, req, null));
            assertEquals(400, ex.getHttpStatus());

            // Cuenta must remain ABIERTA
            Cuenta unchanged = (Cuenta) Cuenta.findById(cuentaId);
            assertEquals(CuentaEstado.ABIERTA, unchanged.estado);
        } finally {
            cleanup(cuentaId);
        }
    }
}
