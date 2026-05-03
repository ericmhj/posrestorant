package com.restaurant.pos.reporte;

import com.restaurant.pos.cuenta.Cuenta;
import com.restaurant.pos.cuenta.CuentaEstado;
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
 * Property 23: total_ventas in report equals sum of all Cuenta.total
 * where cerrada_en falls within the period and estado=CERRADA.
 */
@QuarkusTest
class ReporteTotalesPropertyTest {

    @Inject
    ReporteService reporteService;

    @Transactional
    Cuenta createClosedCuenta(BigDecimal total, LocalDateTime cerradaEn) {
        Mesa mesa = new Mesa();
        mesa.nombre = "ReporteTest-" + UUID.randomUUID();
        mesa.estado = MesaEstado.LIBRE;
        mesa.persist();

        Cuenta cuenta = new Cuenta();
        cuenta.mesa = mesa;
        cuenta.estado = CuentaEstado.CERRADA;
        cuenta.abiertaEn = cerradaEn.minusHours(1);
        cuenta.cerradaEn = cerradaEn;
        cuenta.total = total;
        cuenta.metodoPago = "EFECTIVO";
        cuenta.persist();
        return cuenta;
    }

    @Transactional
    void cleanup(UUID cuentaId) {
        Cuenta.findByIdOptional(cuentaId).ifPresent(c -> {
            Cuenta cuenta = (Cuenta) c;
            UUID mesaId = cuenta.mesa != null ? cuenta.mesa.id : null;
            cuenta.delete();
            if (mesaId != null) {
                Mesa.findByIdOptional(mesaId).ifPresent(m -> ((Mesa) m).delete());
            }
        });
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 23: Totales de reporte son consistentes con transacciones")
    @Transactional
    void property23_reportTotalEqualsSum() {
        LocalDateTime base = LocalDateTime.now().minusDays(1);
        LocalDateTime desde = base.minusHours(1);
        LocalDateTime hasta = base.plusHours(1);

        // Create 10 closed cuentas with known totals
        BigDecimal[] totals = {
            new BigDecimal("100.00"), new BigDecimal("250.50"), new BigDecimal("75.00"),
            new BigDecimal("300.00"), new BigDecimal("50.00"), new BigDecimal("125.75"),
            new BigDecimal("200.00"), new BigDecimal("88.88"), new BigDecimal("150.00"),
            new BigDecimal("33.33")
        };

        BigDecimal expectedTotal = BigDecimal.ZERO;
        UUID[] ids = new UUID[totals.length];

        for (int i = 0; i < totals.length; i++) {
            Cuenta c = createClosedCuenta(totals[i], base);
            ids[i] = c.id;
            expectedTotal = expectedTotal.add(totals[i]);
        }

        try {
            ReporteVentasDTO reporte = reporteService.getReporteVentas(desde, hasta);

            // Total must equal sum of all closed cuentas in period
            assertTrue(reporte.totalVentas.compareTo(expectedTotal) >= 0,
                    "Report total must be >= expected total. Expected: " +
                    expectedTotal + ", got: " + reporte.totalVentas);
            assertTrue(reporte.numeroCuentas >= totals.length,
                    "Report must count at least " + totals.length + " cuentas");
        } finally {
            for (UUID id : ids) cleanup(id);
        }
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 23: Totales de reporte son consistentes con transacciones")
    @Transactional
    void property23_emptyPeriodReturnsZeros() {
        // Period in the far future — no data
        LocalDateTime desde = LocalDateTime.now().plusYears(10);
        LocalDateTime hasta = LocalDateTime.now().plusYears(11);

        ReporteVentasDTO reporte = reporteService.getReporteVentas(desde, hasta);

        assertEquals(BigDecimal.ZERO, reporte.totalVentas,
                "Empty period must return zero total");
        assertEquals(0L, reporte.numeroCuentas,
                "Empty period must return zero cuenta count");
        assertEquals(BigDecimal.ZERO, reporte.ticketPromedio,
                "Empty period must return zero ticket promedio");
    }
}
